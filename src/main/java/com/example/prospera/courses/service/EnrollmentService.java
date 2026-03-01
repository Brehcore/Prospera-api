package com.example.prospera.courses.service;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.repositories.AuthUserRepository;
import com.example.prospera.certificate.domain.Certificate;
import com.example.prospera.certificate.repositories.CertificateRepository;
import com.example.prospera.common.service.AuthorizationService;
import com.example.prospera.courses.domain.Enrollment;
import com.example.prospera.courses.domain.Training;
import com.example.prospera.courses.domain.TrainingRating;
import com.example.prospera.courses.domain.enums.EnrollmentStatus;
import com.example.prospera.courses.domain.enums.TrainingEntityType;
import com.example.prospera.courses.dto.EbookProgressDTO;
import com.example.prospera.courses.dto.EnrollmentResponseDTO;
import com.example.prospera.courses.repositories.EnrollmentRepository;
import com.example.prospera.courses.repositories.TrainingRatingRepository;
import com.example.prospera.courses.repositories.TrainingRepository;
import com.example.prospera.enterprise.domain.Membership;
import com.example.prospera.enterprise.domain.Organization;
import com.example.prospera.enterprise.dto.MemberResponseDTO;
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
    private final CertificateRepository certificateRepository;
    private final TrainingRatingRepository trainingRatingRepository;

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
                BigDecimal.ZERO,
                null,
                null,
                null
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

            Integer myScore = trainingRatingRepository.findByEnrollmentId(enrollment.getId())
                    .map(TrainingRating::getScore)
                    .orElse(null);

            if (training.getEntityType() == TrainingEntityType.EBOOK) {
                EbookProgressDTO progress = progressService.getEbookProgress(user.getId(), training.getId());
                realProgressPercentage = progress.progressPercentage();
            } else if (training.getEntityType() == TrainingEntityType.RECORDED_COURSE) {
                realProgressPercentage = progressService.calculateCourseProgress(enrollment);
            }

            // Verifica se já existe certificado emitido para esta matrícula
            var certificateOpt = certificateRepository.findByEnrollmentId(enrollment.getId());

            UUID certId = certificateOpt.map(Certificate::getId).orElse(null);
            String valCode = certificateOpt.map(Certificate::getValidationCode).orElse(null);

            return new EnrollmentResponseDTO(
                    enrollment.getId(),
                    training.getId(),
                    training.getTitle(),
                    enrollment.getStatus(),
                    enrollment.getEnrolledAt(),
                    training.getCoverImageUrl(),
                    realProgressPercentage,
                    certId,
                    valCode,
                    myScore
            );
        }).collect(Collectors.toList());
    }

}