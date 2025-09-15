package com.example.docgen.auth.api;

import com.example.docgen.auth.api.dto.AuthLoginRequest;
import com.example.docgen.auth.api.dto.AuthResponse;
import com.example.docgen.auth.api.dto.UserRegisterRequest;
import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.jwt.JwtService;
import com.example.docgen.auth.services.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserRegisterRequest request) {
        authenticationService.registerIdentity(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

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
}