package com.example.docgen.auth.services;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.domain.UserProfilePF;
import com.example.docgen.auth.domain.UserProfilePJ;
import com.example.docgen.auth.dto.UserProfileMapperDTO;
import com.example.docgen.auth.dto.UserProfilePessoaFisicaDTO;
import com.example.docgen.auth.dto.UserProfilePessoaJuridicaDTO;
import com.example.docgen.auth.dto.UserProfileResponseDTO;
import com.example.docgen.auth.exceptions.CnpjValidationException;
import com.example.docgen.auth.exceptions.CpfValidationException;
import com.example.docgen.auth.repositories.UserProfilePFRepository;
import com.example.docgen.auth.repositories.UserProfilePJRepository;
import com.example.docgen.common.validation.CnpjValidationService;
import com.example.docgen.common.validation.CpfValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserProfilePFRepository userProfilePFRepository;
    private final UserProfilePJRepository userProfilePJRepository;
    private final CpfValidationService cpfValidationService;
    private final CnpjValidationService cnpjValidationService;

    public UserService(UserProfilePFRepository userProfilePFRepository,
                       UserProfilePJRepository userProfilePJRepository,
                       CpfValidationService cpfValidationService,
                       CnpjValidationService cnpjValidationService) {
        this.userProfilePFRepository = userProfilePFRepository;
        this.userProfilePJRepository = userProfilePJRepository;
        this.cpfValidationService = cpfValidationService;
        this.cnpjValidationService = cnpjValidationService;
    }

    @Transactional
    public UserProfileResponseDTO completeProfilePF(AuthUser user, UserProfilePessoaFisicaDTO dto) {
        // Validação de formato
        if (!cpfValidationService.isValid(dto.getCpf())) {
            throw new CpfValidationException("O CPF informado é inválido.");
        }

        // Regras de negócio
        if (userProfilePFRepository.existsById(user.getId())) {
            throw new IllegalStateException("Usuário já possui um perfil de Pessoa Física.");
        }
        if (userProfilePFRepository.existsByCpf(dto.getCpf())) {
            throw new IllegalStateException("O CPF informado já está em uso por outro usuário.");
        }

        // Criação e persistência
        UserProfilePF profile = new UserProfilePF(user, dto.getCpf(), dto.getFullName(), dto.getBirthDate());
        UserProfilePF savedProfile = userProfilePFRepository.save(profile);

        return UserProfileMapperDTO.fromPfEntity(savedProfile);
    }

    @Transactional
    public UserProfileResponseDTO completeProfilePJ(AuthUser user, UserProfilePessoaJuridicaDTO dto) {
        // Validação de formato
        if (!cnpjValidationService.isValid(dto.getCnpj())) {
            throw new CnpjValidationException("O CNPJ informado é inválido.");
        }

        // Regras de negócio
        if (userProfilePJRepository.existsById(user.getId())) {
            throw new IllegalStateException("Usuário já possui um perfil de Pessoa Jurídica.");
        }
        if (userProfilePJRepository.existsByCnpj(dto.getCnpj())) {
            throw new IllegalStateException("O CNPJ informado já está em uso por outra empresa.");
        }

        // Criação e persistência
        UserProfilePJ profile = new UserProfilePJ(user, dto.getCnpj(), dto.getRazaoSocial(), dto.getNomeFantasia());
        UserProfilePJ savedProfile = userProfilePJRepository.save(profile);

        return UserProfileMapperDTO.fromPjEntity(savedProfile);
    }
}