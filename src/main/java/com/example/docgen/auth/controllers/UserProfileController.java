package com.example.docgen.auth.controllers;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.dto.UserProfilePessoaFisicaDTO;
import com.example.docgen.auth.dto.UserProfilePessoaJuridicaDTO;
import com.example.docgen.auth.dto.UserProfileResponseDTO;
import com.example.docgen.auth.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/profile") // Um novo endpoint base para perfis
public class UserProfileController {

    private final UserService userService;

    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/complete-pf")
    public ResponseEntity<UserProfileResponseDTO> completeProfilePF(
            @AuthenticationPrincipal AuthUser loggedUser, // Pega o usuário logado
            @RequestBody @Valid UserProfilePessoaFisicaDTO profileData) {

        UserProfileResponseDTO completedProfile = userService.completeProfilePF(loggedUser, profileData);
        return ResponseEntity.ok(completedProfile);
    }

    @PostMapping("/complete-pj")
    public ResponseEntity<UserProfileResponseDTO> completeProfilePJ(
            @AuthenticationPrincipal AuthUser loggedUser, // Pega o usuário logado
            @RequestBody @Valid UserProfilePessoaJuridicaDTO profileData) {

        UserProfileResponseDTO completedProfile = userService.completeProfilePJ(loggedUser, profileData);
        return ResponseEntity.ok(completedProfile);
    }
}