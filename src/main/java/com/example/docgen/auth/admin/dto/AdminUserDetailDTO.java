package com.example.docgen.auth.admin.dto;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.common.enums.OrganizationRole;
import com.example.docgen.common.enums.UserRole;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

// DTO para a visão completa de um usuário
public record AdminUserDetailDTO(
        UUID id,
        String email,
        UserRole role,
        boolean enabled,
        PersonalProfileInfo personalProfile,
        List<MembershipInfo> memberships
) {
    public static AdminUserDetailDTO fromEntity(AuthUser user) {
        PersonalProfileInfo pfInfo = (user.getPersonalProfile() != null)
                ? PersonalProfileInfo.fromEntity(user.getPersonalProfile())
                : null;

        List<MembershipInfo> membershipInfo = (user.getMemberships() != null)
                ? user.getMemberships().stream().map(MembershipInfo::fromEntity).collect(Collectors.toList())
                : Collections.emptyList();

        return new AdminUserDetailDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isEnabled(),
                pfInfo,
                membershipInfo
        );
    }

    // Sub-DTOs para aninhar as informações
    public record PersonalProfileInfo(String fullName, String cpf, LocalDate birthDate, String phone) {
        public static PersonalProfileInfo fromEntity(com.example.docgen.auth.domain.UserProfilePF pf) {
            return new PersonalProfileInfo(pf.getFullName(), pf.getCpf(), pf.getBirthDate(), pf.getPhone());
        }
    }

    public record MembershipInfo(UUID organizationId, String organizationName, OrganizationRole role) {
        public static MembershipInfo fromEntity(com.example.docgen.enterprise.domain.Membership m) {
            return new MembershipInfo(m.getOrganization().getId(), m.getOrganization().getRazaoSocial(), m.getRole());
        }
    }
}