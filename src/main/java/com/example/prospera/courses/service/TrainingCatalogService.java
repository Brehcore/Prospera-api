package com.example.prospera.courses.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.common.enums.UserRole;
import com.example.prospera.courses.api.dto.LessonDTO;
import com.example.prospera.courses.api.dto.ModuleDTO;
import com.example.prospera.courses.api.dto.PublicTrainingDTO;
import com.example.prospera.courses.api.dto.SimpleSectorDTO;
import com.example.prospera.courses.api.dto.TrainingCatalogItemDTO;
import com.example.prospera.courses.api.dto.TrainingSummaryDTO;
import com.example.prospera.courses.domain.Enrollment;
import com.example.prospera.courses.domain.Lesson;
import com.example.prospera.courses.domain.Module;
import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.TrainingSectorAssignment;
import com.example.prospera.courses.domain.enums.EnrollmentStatus;
import com.example.prospera.courses.domain.enums.PublicationStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;
import com.example.prospera.courses.domain.enums.TrainingType;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.ModuleRepository;
import com.example.prospera.courses.repositories.TrainingRepository;
import com.example.prospera.courses.repositories.TrainingSectorAssignmentRepository;
import com.example.prospera.enterprise.api.dto.SectorDTO;
import com.example.prospera.enterprise.domain.UserSector;
import com.example.prospera.enterprise.repositories.SectorRepository;
import com.example.prospera.enterprise.repositories.UserSectorRepository;
import com.example.prospera.enterprise.service.SectorService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingCatalogService {

    private final TrainingRepository trainingRepository;
    private final TrainingSectorAssignmentRepository assignmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserSectorRepository userSectorRepository;
    private final SectorRepository sectorRepository;
    private final SectorService sectorService;
    private final LessonService lessonService;
    private final ModuleRepository moduleRepository;

    @Transactional(readOnly = true)
    public List<TrainingCatalogItemDTO> getCatalogForUser(AuthUser user) {
        List<UUID> userSectorIds = userSectorRepository.findByUserId(user.getId()).stream().map(UserSector::getSectorId).toList();

        if (userSectorIds.isEmpty()) return Collections.emptyList();
        List<TrainingSectorAssignment> assignments = assignmentRepository.findBySectorIdIn(userSectorIds);

        Map<UUID, List<TrainingSectorAssignment>> assignmentsByTrainingId = assignments.stream().collect(Collectors.groupingBy(TrainingSectorAssignment::getTrainingId));
        Set<UUID> relevantTrainingIds = assignmentsByTrainingId.keySet();

        if (relevantTrainingIds.isEmpty()) return Collections.emptyList();

        Map<UUID, Training> trainingsById = trainingRepository.findAllById(relevantTrainingIds).stream().collect(Collectors.toMap(Training::getId, Function.identity()));
        Map<UUID, Enrollment> enrollmentsByTrainingId = enrollmentRepository.findByUserIdAndTrainingIdIn(user.getId(), relevantTrainingIds).stream().collect(Collectors.toMap(enrollment -> enrollment.getTraining().getId(), Function.identity()));

        return relevantTrainingIds.stream().map(trainingsById::get).filter(Objects::nonNull).map(training -> {
            List<TrainingSectorAssignment> trainingAssignments = assignmentsByTrainingId.get(training.getId());
            Enrollment enrollment = enrollmentsByTrainingId.get(training.getId());
            TrainingType consolidatedType = trainingAssignments.stream().anyMatch(a -> a.getTrainingType() == TrainingType.COMPULSORY) ? TrainingType.COMPULSORY : TrainingType.ELECTIVE;
            EnrollmentStatus enrollmentStatus = (enrollment != null) ? enrollment.getStatus() : EnrollmentStatus.NOT_ENROLLED;

            return new TrainingCatalogItemDTO(training.getId(),
                    training.getTitle(),
                    training.getDescription(),
                    training.getAuthor(),
                    training.getEntityType().name(),
                    consolidatedType, enrollmentStatus);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TrainingSummaryDTO> findTrainingsBySector(UUID sectorId) {
        List<TrainingSectorAssignment> assignments = assignmentRepository.findBySectorId(sectorId);
        if (assignments.isEmpty()) return Collections.emptyList();
        List<UUID> trainingIds = assignments.stream().map(TrainingSectorAssignment::getTrainingId).collect(Collectors.toList());
        List<Training> trainings = trainingRepository.findAllByIdInAndStatus(trainingIds, PublicationStatus.PUBLISHED);
        return trainings.stream().map(TrainingSummaryDTO::fromEntity).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PublicTrainingDTO> findAllPublishedForPublic() {
        List<Training> trainings = trainingRepository.findByStatus(PublicationStatus.PUBLISHED);
        if (trainings.isEmpty()) {
            return Collections.emptyList();
        }

        Set<UUID> allSectorIds = trainings.stream()
                .flatMap(training -> training.getSectorAssignments().stream())
                .map(TrainingSectorAssignment::getSectorId)
                .collect(Collectors.toSet());

        Map<UUID, String> sectorNamesById = sectorService.findSectorsByIds(allSectorIds).stream()
                .collect(Collectors.toMap(SectorDTO::id, SectorDTO::name));

        return trainings.stream()
                .map(training -> buildPublicTrainingDTO(training, sectorNamesById))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicTrainingDTO findPublishedByIdForPublic(UUID trainingId) {
        Training training = trainingRepository.findByIdAndStatus(trainingId, PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado ou não está publicado."));

        Set<UUID> sectorIds = training.getSectorAssignments().stream()
                .map(TrainingSectorAssignment::getSectorId)
                .collect(Collectors.toSet());

        Map<UUID, String> sectorNamesById = sectorService.findSectorsByIds(sectorIds).stream()
                .collect(Collectors.toMap(SectorDTO::id, SectorDTO::name));

        return buildPublicTrainingDTO(training, sectorNamesById);
    }

    @Transactional(readOnly = true)
    public List<SectorDTO> findAllPublicSectors() {
        return sectorRepository.findAll().stream().map(SectorDTO::fromEntity).collect(Collectors.toList());
    }

    /**
     * Busca a estrutura de módulos e aulas.
     * Permite acesso se for SYSTEM_ADMIN ou se o aluno tiver matrícula ativa.
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> findModulesForStudent(AuthUser user, UUID trainingId) {

        // 1. Verificação de Segurança (Com Bypass para Admin)
        if (user.getRole() != UserRole.SYSTEM_ADMIN) {
            boolean isEnrolled = enrollmentRepository.existsByUserIdAndTrainingId(user.getId(), trainingId);

            if (!isEnrolled) {
                // Se não é admin e não está matriculado, bloqueia.
                throw new org.springframework.security.access.AccessDeniedException("Você não está matriculado neste treinamento.");
            }
        }

        // 2. Buscar os módulos do treinamento
        List<Module> modules = moduleRepository.findAllByCourse_IdOrderByModuleOrder(trainingId);

        // 3. Converter para DTOs
        return modules.stream()
                .map(module -> {
                    List<LessonDTO> lessonDTOs = module.getLessons().stream()
                            .sorted(Comparator.comparingInt(Lesson::getLessonOrder))
                            .map(lesson -> {
                                boolean isCompleted = false;

                                // LÓGICA DE PROGRESSO:
                                // Só verificamos o progresso se NÃO for Admin (ou se o Admin tiver matrícula opcionalmente).
                                // Para simplificar: Admin sempre vê como "não concluído" (ou false), pois ele está apenas auditando.
                                if (user.getRole() != UserRole.SYSTEM_ADMIN) {
                                    isCompleted = lessonService.isLessonCompleted(lesson.getId(), user.getId());
                                }

                                return LessonDTO.fromEntity(lesson, isCompleted);
                            })
                            .toList();

                    return new ModuleDTO(
                            module.getId(),
                            module.getTitle(),
                            module.getModuleOrder(),
                            lessonDTOs
                    );
                })
                .toList();
    }

    public String calculateWorkloadForCertificate(Training training) {
        int totalMinutes = 0;

        if (training.getEntityType() == TrainingEntityType.EBOOK) {
            // Regra: Ebook usa pageCount. Se for nulo, assume 0.
            // Estimativa: 3 minutos por página
            int pages = (training.getPageCount() != null) ? training.getPageCount() : 0;
            totalMinutes = pages * 3;

        } else if (training.getEntityType() == TrainingEntityType.RECORDED_COURSE) {
            // Regra: Vídeo soma a duração das aulas
            // (Supondo que você buscou os módulos e aulas do banco)
            totalMinutes = moduleRepository.calculateTotalDurationByTrainingId(training.getId());
        }

        // --- LÓGICA DE APRESENTAÇÃO ---

        // Opção 1: Arredondar para cima (Padrão Ouro para Certificados)
        // Ex: 150 min = 2.5h -> vira "3 Horas"
        // Ex: 130 min = 2.16h -> vira "3 Horas"
        int hoursRoundedUp = (int) Math.ceil(totalMinutes / 60.0);

        // Garante que nunca mostre 0 horas
        if (hoursRoundedUp < 1) hoursRoundedUp = 1;

        return hoursRoundedUp + " horas";

    /* // Opção 2: Mostrar exato com uma casa decimal (Ex: "2,5 horas")
    double hoursExact = totalMinutes / 60.0;
    if (hoursExact < 1.0) hoursExact = 1.0;
    // Formata trocando ponto por vírgula se for Brasil
    return String.format("%.1f horas", hoursExact).replace(".", ",");
    */
    }

    private PublicTrainingDTO buildPublicTrainingDTO(Training training, Map<UUID, String> sectorNamesById) {
        List<SimpleSectorDTO> sectorDTOs = training.getSectorAssignments().stream()
                .map(assignment -> new SimpleSectorDTO(
                        assignment.getSectorId(),
                        sectorNamesById.getOrDefault(assignment.getSectorId(), "Nome não encontrado")
                ))
                .toList();
        return new PublicTrainingDTO(
                training.getId(),
                training.getTitle(),
                training.getAuthor(),
                training.getDescription(),
                training.getCoverImageUrl(),
                training.getEntityType(),
                sectorDTOs
        );
    }

}