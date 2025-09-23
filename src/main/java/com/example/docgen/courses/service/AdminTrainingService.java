package com.example.docgen.courses.service;

import com.example.docgen.courses.api.dto.*;
import com.example.docgen.courses.domain.*;
import com.example.docgen.courses.domain.Module;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingEntityType;
import com.example.docgen.courses.repositories.LessonRepository;
import com.example.docgen.courses.repositories.ModuleRepository;
import com.example.docgen.courses.repositories.TrainingRepository;
import com.example.docgen.courses.repositories.TrainingSectorAssignmentRepository;
import com.example.docgen.courses.service.exception.SectorNotFoundException;
import com.example.docgen.enterprise.api.dto.SectorDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient; // Import correto
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTrainingService {

    private final TrainingRepository trainingRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final TrainingSectorAssignmentRepository assignmentRepository;
    private final FileStorageService fileStorageService;
    // CORREÇÃO 1: Injetando o BEAN WebClient, não a classe de configuração.
    private final WebClient enterpriseWebClient;


    /**
     * Cria um novo "invólucro" de treinamento.
     */
    @Transactional
    public TrainingDTO createTraining(TrainingCreateRequest dto) {
        Training training;
        switch (dto.entityType()) {
            case EBOOK -> training = new EbookTraining();
            case RECORDED_COURSE -> training = new RecordedCourse();
            case LIVE_TRAINING -> training = new LiveTraining();
            default -> throw new IllegalArgumentException("Tipo de treinamento inválido: " + dto.entityType());
        }

        training.setTitle(dto.title());
        training.setDescription(dto.description());
        training.setAuthor(dto.author());
        training.setOrganizationId(dto.organizationId());
        training.setEntityType(dto.entityType());
        training.setStatus(PublicationStatus.DRAFT);

        Training savedTraining = trainingRepository.save(training);
        return TrainingDTO.fromEntity(savedTraining);
    }

    /**
     * Associa um treinamento a um setor.
     */
    @Transactional
    public void assignTrainingToSector(UUID trainingId, TrainingSectorAssignmentRequest dto) {
        if (!trainingRepository.existsById(trainingId)) {
            throw new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId);
        }

        // CORREÇÃO 2: Validação feita via API, respeitando a arquitetura.
        validateSectorExists(dto.sectorId()).block();

        TrainingSectorAssignment assignment = new TrainingSectorAssignment();
        assignment.setTrainingId(trainingId);
        assignment.setSectorId(dto.sectorId());
        assignment.setTrainingType(dto.trainingType());
        assignment.setLegalBasis(dto.legalBasis());

        assignmentRepository.save(assignment);
    }

    /**
     * Adiciona um módulo a um treinamento do tipo CURSO GRAVADO.
     */
    @Transactional
    public ModuleDTO addModuleToTraining(UUID trainingId, ModuleCreateRequest dto) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId));

        if (!(training instanceof RecordedCourse recordedCourse)) {
            throw new IllegalArgumentException("Módulos só podem ser adicionados a treinamentos do tipo 'RECORDED_COURSE'.");
        }

        Module newModule = Module.builder()
                .course(recordedCourse)
                .title(dto.title())
                .moduleOrder(dto.moduleOrder())
                .build();

        Module savedModule = moduleRepository.save(newModule);
        return ModuleDTO.fromEntity(savedModule);
    }

    /**
     * Adiciona uma lição a um módulo.
     */
    @Transactional
    public LessonDTO addLessonToModule(UUID moduleId, LessonCreateRequest dto) {
        Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new EntityNotFoundException("Módulo não encontrado com o ID: " + moduleId));

        Lesson newLesson = Lesson.builder()
                .module(module)
                .title(dto.title())
                .content(dto.content())
                .lessonOrder(dto.lessonOrder())
                .build();

        Lesson savedLesson = lessonRepository.save(newLesson);
        return LessonDTO.fromEntity(savedLesson);
    }

    /**
     * Altera o status de um treinamento (ex: de DRAFT para PUBLISHED).
     */
    @Transactional
    public void changeTrainingStatus(UUID trainingId, PublicationStatus status) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId));
        training.setStatus(status);
        trainingRepository.save(training);
    }

    @Transactional
    public void setEbookFile(UUID trainingId, MultipartFile file) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado: " + trainingId));

        if (!(training instanceof EbookTraining ebook)) {
            throw new IllegalArgumentException("O arquivo só pode ser associado a um treinamento do tipo EBOOK.");
        }

        try {
            // 1. Salva o arquivo e obtém o caminho/chave
            String filePath = fileStorageService.save(file);

            // 2. Extrai o número de páginas
            PDDocument document = PDDocument.load(file.getInputStream());
            int pageCount = document.getNumberOfPages();
            document.close();

            // 3. Atualiza a entidade
            ebook.setFilePath(filePath);
            ebook.setTotalPages(pageCount);
            trainingRepository.save(ebook);

        } catch (IOException e) {
            throw new RuntimeException("Falha ao processar o arquivo PDF.", e);
        }
    }

    /**
     * Retorna a lista de treinamentos que um ORG_ADMIN pode atribuir.
     */
    @Transactional(readOnly = true)
    public List<TrainingSummaryDTO> getAssignableTrainingsForOrg(UUID organizationId) {
        // CORREÇÃO 3: A chamada agora funciona pois usa o WebClient injetado corretamente.
        List<SectorDTO> orgSectors = enterpriseWebClient.get()
                .uri("/admin/organizations/{orgId}/sectors", organizationId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<SectorDTO>>() {
                })
                .block();

        List<UUID> orgSectorIds = (orgSectors != null)
                ? orgSectors.stream().map(SectorDTO::id).toList()
                : Collections.emptyList();

        List<TrainingSectorAssignment> assignments = assignmentRepository.findBySectorIdIn(orgSectorIds);
        Set<UUID> sectorTrainingIds = assignments.stream().map(TrainingSectorAssignment::getTrainingId).collect(Collectors.toSet());
        List<Training> sectorTrainings = trainingRepository.findAllById(sectorTrainingIds);

        List<Training> universalTrainings = trainingRepository.findUniversalPublishedTrainings();

        Set<Training> allAssignable = new HashSet<>(sectorTrainings);
        allAssignable.addAll(universalTrainings);

        return allAssignable.stream()
                .map(TrainingSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Busca todos os treinamentos, com um filtro opcional por tipo.
     *
     * @param type O tipo de treinamento para filtrar (EBOOK, COURSE, etc.). Pode ser nulo.
     * @return Uma lista de DTOs resumidos dos treinamentos.
     */
    @Transactional(readOnly = true)
    public List<TrainingSummaryDTO> findAll(TrainingEntityType type) {
        List<Training> trainings;
        if (type != null) {
            // Se um tipo for especificado, filtra por ele
            trainings = trainingRepository.findByEntityType(type);
        } else {
            // Se nenhum tipo for especificado, retorna todos
            trainings = trainingRepository.findAll();
        }

        return trainings.stream()
                .map(TrainingSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // --- MÉTODO PRIVADO DE VALIDAÇÃO (agora funcional) ---
    private Mono<Void> validateSectorExists(UUID sectorId) {
        // CORREÇÃO 4: Esta chamada agora funciona corretamente.
        return enterpriseWebClient.get()
                .uri("/admin/sectors/{id}", sectorId)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND,
                        clientResponse -> Mono.error(new SectorNotFoundException("Setor com ID " + sectorId + " não encontrado.")))
                .bodyToMono(Void.class);
    }
}