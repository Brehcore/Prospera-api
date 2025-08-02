package com.example.docgen.controllers;

import com.example.docgen.dto.AuthenticationRequestDTO;
import com.example.docgen.dto.AuthenticationResponseDTO;
import com.example.docgen.dto.UserProfileResponseDTO;
import com.example.docgen.jwt.JwtService;
import com.example.docgen.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponseDTO> login(@RequestBody AuthenticationRequestDTO data) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(data.getEmail(), data.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String jwtToken = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(new AuthenticationResponseDTO(jwtToken, userDetails.getUsername()));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthenticationResponseDTO(null, "Usuário não encontrado."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthenticationResponseDTO(null, "Credenciais inválidas. Verifique e tente novamente."));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponseDTO> getUserProfile(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .map(user -> ResponseEntity.ok(new UserProfileResponseDTO(user))) // CONSTRUTOR COM OBJETO USER
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new UserProfileResponseDTO("Usuário não encontrado."))); // CONSTRUTOR COM MENSAGEM DE ERRO
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new UserProfileResponseDTO("Não autenticado ou sessão inválida.")); // CONSTRUTOR COM MENSAGEM DE ERRO
    }
}