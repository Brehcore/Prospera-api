package com.example.prospera.auth.api.controllers;

import com.example.prospera.auth.api.dto.AuthLoginRequest;
import com.example.prospera.auth.api.dto.AuthResponse;
import com.example.prospera.auth.api.dto.ChangePasswordRequest;
import com.example.prospera.auth.api.dto.ForgotPasswordRequest;
import com.example.prospera.auth.api.dto.PasswordResetRequest;
import com.example.prospera.auth.api.dto.UserRegisterRequest;
import com.example.prospera.auth.domain.AuthUser;
import com.example.prospera.auth.dto.ConfirmEmailChangeRequest;
import com.example.prospera.auth.dto.InitiateEmailChangeRequest;
import com.example.prospera.auth.jwt.JwtService;
import com.example.prospera.auth.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador responsável por gerenciar operações de autenticação e registro de usuários.
 * Fornece endpoints para registro de novos usuários e autenticação via login.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Registra um novo usuário no sistema.
     *
     * @param request DTO contendo as informações necessárias para registro do usuário
     * @return ResponseEntity com status 201 (CREATED) em caso de sucesso
     */
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserRegisterRequest request) {
        authenticationService.registerIdentity(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Autentica um usuário e gera um token JWT.
     *
     * @param request DTO contendo credenciais do usuário (email e senha)
     * @return ResponseEntity contendo o token JWT e informações do usuário autenticado
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest request) {
        // 1. Autentica o usuário usando o AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        // 2. Se a autenticação for bem-sucedida, pega os detalhes do usuário
        var user = (AuthUser) authentication.getPrincipal();

        // 3. Gera o token JWT
        String token = jwtService.generateToken(user);
        long expirationMillis = jwtService.getJwtExpiration();

        // 4. Monta e retorna a resposta
        AuthResponse response = AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .expiresIn(expirationMillis / 1000)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid PasswordResetRequest request) {
        authenticationService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()") // O SecurityConfig já garante isso para rotas não públicas, mas reforçar é bom
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal AuthUser user, // Pega o usuário do Token JWT
            @RequestBody @Valid ChangePasswordRequest request) {

        // Validação simples de confirmação (pode ser feita no Service ou DTO validator também)
        if (!request.newPassword().equals(request.confirmationPassword())) {
            throw new IllegalArgumentException("A nova senha e a confirmação não coincidem.");
        }

        authenticationService.changePassword(user.getId(), request.currentPassword(), request.newPassword());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/initiate-change")
    public ResponseEntity<String> initiateEmailChange(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid InitiateEmailChangeRequest request) {

        authenticationService.initiateEmailChange(user.getId(), request.currentEmail(), request.newEmail());

        return ResponseEntity.ok("Código de verificação enviado para o seu e-mail atual.");
    }

    @PostMapping("/email/confirm-change")
    public ResponseEntity<Void> confirmEmailChange(
            @AuthenticationPrincipal AuthUser user,
            @RequestBody @Valid ConfirmEmailChangeRequest request) {

        authenticationService.confirmEmailChange(user.getId(), request.code());

        // Frontend deve forçar logout aqui
        return ResponseEntity.ok().build();
    }
}