package com.example.prospera.auth.dto;

import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.domain.UserProfilePF;
import lombok.Getter;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class ProfileMeResponseDTO {

    private final UUID userId;
    private final String email;
    private final String role;
    private final PersonalProfileData personalProfile;
    private final List<OrganizationInfo> organizations;

    // DTO aninhado para os dados de Perfil Pessoal
    @Getter
    public static class PersonalProfileData {
        private final String fullName;
        private final String cpf;
        private final String birthDate;
        private final String phone;

        public PersonalProfileData(UserProfilePF pf) {
            this.fullName = pf.getFullName();
            this.cpf = maskCpf(pf.getCpf());
            this.birthDate = maskBirthDate(pf.getBirthDate().toString());
            this.phone = maskPhone(pf.getPhone());
        }

        // --- MÉTODOS DE MASCARAMENTO ---
        private String maskCpf(String cpf) {
            if (cpf == null || cpf.length() != 11) return "Inválido";
            return "***." + cpf.substring(3, 6) + "." + cpf.substring(6, 9) + "-**";
        }

        private String maskBirthDate(String date) { // Formato AAAA-MM-DD
            if (date == null || date.length() < 7) return "";
            return "****-**-" + date.substring(8);
        }

        private String maskPhone(String phone) {
            if (phone == null || phone.length() < 4) return "";
            return "(" + phone.substring(0, 2) + ") *****-" + phone.substring(phone.length() - 4);
        }
    }

    // DTO aninhado para informações da Organização
    @Getter
    public static class OrganizationInfo {
        private final UUID membershipId;
        private final UUID organizationId;
        private final String razaoSocial;
        private final String yourRole;

        public OrganizationInfo(UUID membershipId, UUID organizationId, String name, String role) {
            this.membershipId = membershipId;
            this.organizationId = organizationId;
            this.razaoSocial = name;
            this.yourRole = role;
        }
    }

    // Construtor Principal
    public ProfileMeResponseDTO(AuthUser user) {
        this.userId = user.getId();
        this.email = maskEmail(user.getEmail());
        this.role = user.getRole().name();

        if (user.getPersonalProfile() != null) {
            this.personalProfile = new PersonalProfileData(user.getPersonalProfile());
        } else {
            this.personalProfile = null;
        }

        this.organizations = user.getMemberships().stream()
                .map(m -> new OrganizationInfo(
                        m.getId(),
                        m.getOrganization().getId(),
                        m.getOrganization().getRazaoSocial(),
                        m.getRole().name()
                ))
                .collect(Collectors.toList());
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "";
        int atIndex = email.indexOf("@");
        String prefix = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (prefix.length() <= 3) {
            return prefix.charAt(0) + "**" + domain;
        }
        return prefix.substring(0, 3) + "***" + domain;
    }
}