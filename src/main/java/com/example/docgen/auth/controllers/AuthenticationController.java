package com.example.docgen.auth.controllers;

import com.example.docgen.auth.domain.AuthUser;
import com.example.docgen.auth.dto.AuthenticationRequestDTO;
import com.example.docgen.auth.dto.AuthenticationResponseDTO;
import com.example.docgen.auth.dto.UserMapperDTO;
import com.example.docgen.auth.dto.UserRequestDTO;
import com.example.docgen.auth.dto.UserResponseDTO;
import com.example.docgen.auth.jwt.JwtService;
import com.example.docgen.auth.services.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthenticationController(AuthenticationService authenticationService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid UserRequestDTO data) {
        AuthUser newUser = authenticationService.register(data);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapperDTO.toDto(newUser));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody @Valid AuthenticationRequestDTO data) {
        // Autentica o usuário
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword())
        );

        // Se a autenticação for bem-sucedida, busca o usuário e gera o token
        var user = (AuthUser) authenticationService.loadUserByUsername(data.getEmail());
        String jwtToken = jwtService.generateToken(user);

        return ResponseEntity.ok(new AuthenticationResponseDTO(jwtToken, user.getEmail()));
    }
}