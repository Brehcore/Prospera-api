package com.example.docgen.courses.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.courses.api.dto.PublicTrainingDTO;
import com.example.docgen.courses.api.dto.TrainingCatalogItemDTO;
import com.example.docgen.courses.api.dto.TrainingSummaryDTO;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.Training;
import com.example.docgen.courses.domain.TrainingSectorAssignment;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;
import com.example.docgen.courses.domain.enums.PublicationStatus;
import com.example.docgen.courses.domain.enums.TrainingType;
import com.example.docgen.courses.repositories.EnrollmentRepository;
import com.example.docgen.courses.repositories.TrainingRepository;
import com.example.docgen.courses.repositories.TrainingSectorAssignmentRepository;
import com.example.docgen.enterprise.domain.UserSector;
import com.example.docgen.enterprise.repositories.UserSectorRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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

    @Transactional(readOnly = true)
    public List<TrainingCatalogItemDTO> getCatalogForUser(AuthUser user) {
        List<UUID> userSectorIds = userSectorRepository.findByUserId(user.getId())
                .stream()
                .map(UserSector::getSectorId)
                .toList();

        if (userSectorIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<TrainingSectorAssignment> assignments = assignmentRepository.findBySectorIdIn(userSectorIds);

        Map<UUID, List<TrainingSectorAssignment>> assignmentsByTrainingId = assignments.stream()
                .collect(Collectors.groupingBy(TrainingSectorAssignment::getTrainingId));

        Set<UUID> relevantTrainingIds = assignmentsByTrainingId.keySet();
        if (relevantTrainingIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<UUID, Training> trainingsById = trainingRepository.findAllById(relevantTrainingIds).stream()
                .collect(Collectors.toMap(Training::getId, Function.identity()));

        // CORREÇÃO 1: Chamando o método do repositório com o nome correto
        Map<UUID, Enrollment> enrollmentsByTrainingId = enrollmentRepository.findByUserIdAndTrainingIdIn(user.getId(), relevantTrainingIds).stream()
                .collect(Collectors.toMap(enrollment -> enrollment.getTraining().getId(), Function.identity())); // E usando .getTraining()

        return relevantTrainingIds.stream()
                .map(trainingsById::get) // Mapeia o ID para a entidade Training
                .filter(Objects::nonNull) // CORREÇÃO 2: Filtra nulos para evitar NullPointerException
                .map(training -> {
                    List<TrainingSectorAssignment> trainingAssignments = assignmentsByTrainingId.get(training.getId());
                    Enrollment enrollment = enrollmentsByTrainingId.get(training.getId());

                    TrainingType consolidatedType = trainingAssignments.stream()
                            .anyMatch(a -> a.getTrainingType() == TrainingType.COMPULSORY)
                            ? TrainingType.COMPULSORY
                            : TrainingType.ELECTIVE;

                    EnrollmentStatus enrollmentStatus = (enrollment != null)
                            ? enrollment.getStatus()
                            : EnrollmentStatus.NOT_ENROLLED;

                    return new TrainingCatalogItemDTO(
                            training.getId(),
                            training.getTitle(),
                            training.getDescription(),
                            training.getAuthor(),
                            training.getEntityType().name(),
                            consolidatedType,
                            enrollmentStatus
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * NOVO MÉTODO: Encontra todos os treinamentos publicados de um setor.
     */
    @Transactional(readOnly = true)
    public List<TrainingSummaryDTO> findTrainingsBySector(UUID sectorId) {
        // 1. Encontra todas as associações para o setor informado.
        List<TrainingSectorAssignment> assignments = assignmentRepository.findBySectorId(sectorId);

        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Extrai os IDs dos treinamentos a partir das associações.
        List<UUID> trainingIds = assignments.stream()
                .map(TrainingSectorAssignment::getTrainingId)
                .collect(Collectors.toList());

        // 3. Busca todos os treinamentos publicados que correspondem a esses IDs.
        List<Training> trainings = trainingRepository.findAllByIdInAndStatus(trainingIds, PublicationStatus.PUBLISHED);

        // 4. Converte para DTO e retorna.
        return trainings.stream()
                .map(TrainingSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PublicTrainingDTO> findAllPublishedForPublic() {
        // Busca apenas treinamentos com status PUBLISHED
        List<Training> trainings = trainingRepository.findByStatus(PublicationStatus.PUBLISHED);
        return trainings.stream()
                .map(PublicTrainingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PublicTrainingDTO findPublishedByIdForPublic(UUID trainingId) {
        Training training = trainingRepository.findByIdAndStatus(trainingId, PublicationStatus.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado ou não está publicado."));
        return PublicTrainingDTO.fromEntity(training);
    }
}