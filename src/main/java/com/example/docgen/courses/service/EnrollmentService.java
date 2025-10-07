package com.example.docgen.courses.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.common.service.AuthorizationService;
import com.example.docgen.courses.api.dto.EbookProgressDTO;
import com.example.docgen.courses.api.dto.EnrollmentResponseDTO;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.Training;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;
import com.example.docgen.courses.domain.enums.TrainingEntityType;
import com.example.docgen.courses.repositories.EnrollmentRepository;
import com.example.docgen.courses.repositories.TrainingRepository;
import com.example.docgen.enterprise.api.dto.MemberResponseDTO;
import com.example.docgen.enterprise.domain.Membership;
import com.example.docgen.enterprise.domain.Organization;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final TrainingRepository trainingRepository;
    private final AuthUserRepository authUserRepository;
    private final ProgressService progressService;
    private final AuthorizationService authorizationService;

    @Transactional
    public EnrollmentResponseDTO enrollUserInTraining(AuthUser user, UUID trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId));

        if (enrollmentRepository.existsByUserAndTraining(user, training)) {
            throw new IllegalStateException("Usuário já matriculado neste treinamento.");
        }

        if (training.getOrganizationId() != null) {
            authorizationService.checkIsMemberOfOrg(user, training.getOrganizationId());
        }

        Enrollment newEnrollment = Enrollment.builder()
                .user(user)
                .training(training)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        Enrollment savedEnrollment = enrollmentRepository.save(newEnrollment);

        // 2. Crie e retorne o DTO diretamente do serviço.
        // O progresso de uma nova matrícula é sempre ZERO.
        return new EnrollmentResponseDTO(
                savedEnrollment.getId(),
                savedEnrollment.getTraining().getId(),
                savedEnrollment.getTraining().getTitle(),
                savedEnrollment.getStatus(),
                savedEnrollment.getEnrolledAt(),
                savedEnrollment.getTraining().getCoverImageUrl(),
                BigDecimal.ZERO
        );
    }

    @Transactional
    public void enrollMembersInTraining(UUID trainingId, List<UUID> memberUserIds, Organization organization) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado."));

        List<AuthUser> usersToEnroll = authUserRepository.findAllById(memberUserIds);

        for (AuthUser user : usersToEnroll) {
            authorizationService.checkIsMemberOfOrg(user, organization.getId());
        }

        Set<UUID> alreadyEnrolledUserIds = enrollmentRepository.findEnrolledUserIdsByTrainingAndUserIds(trainingId, memberUserIds);

        List<Enrollment> newEnrollments = new ArrayList<>();
        for (AuthUser user : usersToEnroll) {
            if (!alreadyEnrolledUserIds.contains(user.getId())) {
                newEnrollments.add(Enrollment.builder()
                        .user(user)
                        .training(training)
                        .status(EnrollmentStatus.ACTIVE)
                        .sponsoredBy(organization)
                        .build());
            }
        }

        if (!newEnrollments.isEmpty()) {
            enrollmentRepository.saveAll(newEnrollments);
        }
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDTO> getEnrolledMembers(UUID organizationId, UUID trainingId) {
        List<Membership> memberships = enrollmentRepository.findMembershipsByOrganizationAndTraining(organizationId, trainingId);
        return memberships.stream()
                .map(MemberResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponseDTO> findEnrollmentsForUser(AuthUser user) {
        List<Enrollment> enrollments = enrollmentRepository.findByUserWithTrainingDetails(user);

        return enrollments.stream().map(enrollment -> {
            BigDecimal realProgressPercentage = BigDecimal.ZERO;
            Training training = enrollment.getTraining();

            if (training.getEntityType() == TrainingEntityType.EBOOK) {
                EbookProgressDTO progress = progressService.getEbookProgress(user.getId(), training.getId());
                realProgressPercentage = progress.progressPercentage();
            } else if (training.getEntityType() == TrainingEntityType.RECORDED_COURSE) {
                realProgressPercentage = progressService.calculateCourseProgress(enrollment);
            }
            return new EnrollmentResponseDTO(
                    enrollment.getId(),
                    training.getId(),
                    training.getTitle(),
                    enrollment.getStatus(),
                    enrollment.getEnrolledAt(),
                    training.getCoverImageUrl(),
                    realProgressPercentage
            );
        }).collect(Collectors.toList());
    }

}