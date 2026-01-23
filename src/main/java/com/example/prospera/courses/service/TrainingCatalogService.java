package com.example.prospera.courses.service;

import com.example.prospera.auth.domain.AuthUser;
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
     * Busca a estrutura de módulos e aulas para um aluno, verificando se ele tem permissão (matrícula).
     */
    @Transactional(readOnly = true)
    public List<ModuleDTO> findModulesForStudent(AuthUser user, UUID trainingId) {
        // 1. Segurança: Verificar se o usuário está matriculado neste curso
        // (Assumindo que EnrollmentRepository tenha este método ou similar)
        boolean isEnrolled = enrollmentRepository.existsByUserIdAndTrainingId(user.getId(), trainingId);

        if (!isEnrolled) {
            throw new org.springframework.security.access.AccessDeniedException("Você não está matriculado neste treinamento.");
        }

        // 2. Buscar os módulos do treinamento (ordenados por ordem de exibição)
        // (Assumindo que ModuleRepository tenha findByTrainingIdOrderByOrderIndex ou similar)
        List<Module> modules = moduleRepository.findAllByCourse_IdOrderByModuleOrder(trainingId);

        // 3. Converter para DTOs e preencher o status de conclusão das aulas
        return modules.stream()
                .map(module -> {
                    // Mapeia as lições dentro do módulo
                    List<LessonDTO> lessonDTOs = module.getLessons().stream()
                            .sorted(Comparator.comparingInt(Lesson::getLessonOrder)) // Garante ordem das aulas
                            .map(lesson -> {
                                // Verifica se o aluno já completou esta aula específica
                                boolean isCompleted = lessonService.isLessonCompleted(lesson.getId(), user.getId());
                                // Retorna o DTO com o status correto e URL do vídeo
                                return LessonDTO.fromEntity(lesson, isCompleted);
                            })
                            .toList();

                    // Retorna o DTO do módulo preenchido
                    return new ModuleDTO(
                            module.getId(),
                            module.getTitle(),
                            module.getModuleOrder(),
                            lessonDTOs
                    );
                })
                .toList();
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