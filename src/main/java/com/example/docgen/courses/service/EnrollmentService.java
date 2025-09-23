package com.example.docgen.courses.service;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.repositories.AuthUserRepository;
import com.example.docgen.courses.domain.Training;
import com.example.docgen.courses.domain.Enrollment;
import com.example.docgen.courses.domain.enums.EnrollmentStatus;
import com.example.docgen.courses.repositories.TrainingRepository;
import com.example.docgen.courses.repositories.EnrollmentRepository;
import com.example.docgen.enterprise.domain.Organization;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final TrainingRepository trainingRepository;
    private final AuthUserRepository authUserRepository;

    // CORREÇÃO: Método renomeado para 'enrollUserInTraining' para consistência
    @Transactional
    public Enrollment enrollUserInTraining(AuthUser user, UUID trainingId) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado com o ID: " + trainingId));

        // FIX 1: Adicionada de volta a verificação de matrícula duplicada
        if (enrollmentRepository.existsByUserAndTraining(user, training)) {
            throw new IllegalStateException("Usuário já matriculado neste treinamento.");
        }

        // LÓGICA CORRIGIDA: Verifica se o usuário tem um membership na organização do treinamento
        if (training.getOrganizationId() != null) {
            boolean isMember = user.getMemberships().stream()
                    .anyMatch(membership -> membership.getOrganization().getId().equals(training.getOrganizationId()));

            if (!isMember) {
                throw new AccessDeniedException("Você não é membro da organização dona deste treinamento.");
            }
        }

        // CORREÇÃO: O builder da entidade Enrollment usa o campo 'training'
        Enrollment newEnrollment = Enrollment.builder()
                .user(user)
                .training(training) // Corrigido de .course(training)
                .status(EnrollmentStatus.ACTIVE) // Sugestão: ACTIVE é mais claro que IN_PROGRESS
                .progressPercentage(BigDecimal.ZERO)
                .build();

        return enrollmentRepository.save(newEnrollment);
    }

    // Enpoint de matrícula em massa
    @Transactional
    public void enrollMembersInTraining(UUID trainingId, List<UUID> memberUserIds, Organization organization) {
        Training training = trainingRepository.findById(trainingId)
                .orElseThrow(() -> new EntityNotFoundException("Treinamento não encontrado."));

        // FIX 2: Adicionada a busca pelos usuários a serem matriculados
        List<AuthUser> usersToEnroll = authUserRepository.findAllById(memberUserIds);

        // FIX 3: Adicionada de volta a verificação de segurança
        for (AuthUser user : usersToEnroll) {
            boolean isMember = user.getMemberships().stream()
                    .anyMatch(m -> m.getOrganization().getId().equals(organization.getId()));
            if (!isMember) {
                throw new AccessDeniedException("Tentativa de matricular um usuário que não pertence à organização: " + user.getEmail());
            }
        }

        // FIX 4: Corrigida a chamada para o método correto do repositório
        Set<UUID> alreadyEnrolledUserIds = enrollmentRepository.findEnrolledUserIdsByTrainingAndUserIds(trainingId, memberUserIds);

        List<Enrollment> newEnrollments = new ArrayList<>();
        for (AuthUser user : usersToEnroll) {
            if (!alreadyEnrolledUserIds.contains(user.getId())) {
                // FIX 5: Preenchido o builder com os dados necessários
                newEnrollments.add(Enrollment.builder()
                        .user(user)
                        .training(training)
                        .status(EnrollmentStatus.ACTIVE)
                        .progressPercentage(BigDecimal.ZERO)
                        .sponsoredBy(organization) // Importante para saber quem pagou
                        .build());
            }
        }

        if (!newEnrollments.isEmpty()) {
            enrollmentRepository.saveAll(newEnrollments);
        }
    }
}