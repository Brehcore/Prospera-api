package com.example.prospera.courses.service;

import com.example.prospera.courses.api.dto.LessonCreateRequest;
import com.example.prospera.courses.api.dto.LessonDTO;
import com.example.prospera.courses.api.dto.ModuleCreateRequest;
import com.example.prospera.courses.api.dto.ModuleDTO;
import com.example.prospera.courses.api.dto.TrainingCreateRequest;
import com.example.prospera.courses.api.dto.TrainingDTO;
import com.example.prospera.courses.api.dto.TrainingDetailDTO;
import com.example.prospera.courses.api.dto.TrainingSectorAssignmentRequest;
import com.example.prospera.courses.api.dto.TrainingSummaryDTO;
import com.example.prospera.courses.api.dto.TrainingUpdateRequest;
import com.example.prospera.courses.domain.EbookTraining;
import com.example.prospera.courses.domain.Lesson;
import com.example.prospera.courses.domain.LiveTraining;
import com.example.prospera.courses.domain.Module;
import com.example.prospera.courses.domain.RecordedCourse;
import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.TrainingSectorAssignment;
import com.example.prospera.courses.domain.enums.PublicationStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.LessonRepository;
import com.example.prospera.courses.repositories.ModuleRepository;
import com.example.prospera.courses.repositories.TrainingRepository;
import com.example.prospera.courses.repositories.TrainingSectorAssignmentRepository;
import com.example.prospera.enterprise.api.dto.SectorDTO;
import com.example.prospera.enterprise.service.SectorAssignmentService;
import com.example.prospera.exception.SectorNotFoundException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminTrainingService {

    private final TrainingRepository trainingRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final TrainingSectorAssignmentRepository assignmentRepository;
    private final FileStorageService fileStorageService;
    private final WebClient enterpriseWebClient;
    private final EnrollmentRepository enrollmentRepository;
    private final SectorAssignmentService sectorAssignmentService;


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
            ebook.setFileUploadedAt(OffsetDateTime.now());

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

        List<SectorDTO> orgSectors = sectorAssignmentService.getSectorsForOrganization(organizationId);

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

    // =======================================================================================
    // == NOVOS MÉTODOS PARA O CRUD COMPLETO DE TREINAMENTOS                                ==
    // =======================================================================================

    /**
     * Busca um treinamento pelo ID e o converte para um DTO detalhado.
     * Usado pelo endpoint GET /admin/trainings/{trainingId}.
     */
    @Transactional(readOnly = true)
    public TrainingDetailDTO getTrainingById(UUID trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId));

        // E garanta que ele chama o DTO correto aqui
        return TrainingDetailDTO.fromEntity(training);
    }

    /**
     * Atualiza os dados de um treinamento existente.
     * Usado pelo endpoint PUT /admin/trainings/{trainingId}.
     */
    @Transactional
    public TrainingDTO updateTraining(UUID trainingId, TrainingUpdateRequest dto) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId));

        // Atualiza os campos básicos
        training.setTitle(dto.title());
        training.setDescription(dto.description());
        training.setAuthor(dto.author());
        // Adicione outros campos que podem ser atualizados

        Training updatedTraining = trainingRepository.save(training);
        return TrainingDTO.fromEntity(updatedTraining);
    }

    /**
     * Exclui um treinamento, mas apenas se ele não tiver dados associados.
     * Usado pelo endpoint DELETE /admin/trainings/{trainingId}.
     */
    @Transactional
    public void deleteTraining(UUID trainingId) {
        // Validação 1: O treinamento existe?
        if (!trainingRepository.existsById(trainingId)) {
            throw new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId);
        }

        // Validação 2 (SEGURANÇA): Verifica se há matrículas associadas.
        if (enrollmentRepository.existsByTrainingId(trainingId)) {
            throw new IllegalStateException("Não é possível excluir este treinamento pois existem membros matriculados.");
        }

        // Validação 3 (SEGURANÇA): Verifica se há módulos associados (se for um curso).
        if (moduleRepository.existsByCourseId(trainingId)) {
            throw new IllegalStateException("Não é possível excluir este treinamento pois existem módulos associados. Exclua os módulos primeiro.");
        }

        // Validação 4 (SEGURANÇA): Verifica se há setores associados.
        if (assignmentRepository.existsByTrainingId(trainingId)) {
            throw new IllegalStateException("Não é possível excluir este treinamento pois ele está associado a setores.");
        }

        // Se todas as validações passaram, a exclusão é segura.
        trainingRepository.deleteById(trainingId);
    }

    @Transactional
    public void setCoverImage(UUID trainingId, MultipartFile file) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado: " + trainingId));

        // 1. Salva o arquivo usando o mesmo serviço de antes e obtém o nome único.
        String filename = fileStorageService.save(file);

        // 2. Constrói a URL completa para acessar o arquivo.
        String imageUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath() // Pega a base da URL (ex: http://localhost:8080)
                .path("/stream/images/") // Adiciona o caminho do endpoint de streaming
                .path(filename)          // Adiciona o nome do arquivo
                .toUriString();          // Converte para String

        // 3. Salva a URL completa no banco de dados.
        training.setCoverImageUrl(imageUrl);
        trainingRepository.save(training);
    }

    /**
     * Desvincula um treinamento de um setor no catálogo global.
     */
    @Transactional
    public void unassignTrainingFromSector(UUID trainingId, UUID sectorId) {
        // Opcional: Adicionar validações para verificar se os IDs existem antes de deletar.

        // Chama o novo método do repositório para executar a exclusão.
        assignmentRepository.deleteByTrainingIdAndSectorId(trainingId, sectorId);
    }
}