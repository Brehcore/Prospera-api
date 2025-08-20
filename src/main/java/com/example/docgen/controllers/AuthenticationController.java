package com.example.docgen.controllers;

import com.example.docgen.dto.AuthenticationRequestDTO;
import com.example.docgen.dto.AuthenticationResponseDTO;
import com.example.docgen.dto.UserProfileResponseDTO;
import com.example.docgen.dto.UserRequestDTO;
import com.example.docgen.dto.UserUpdateDTO;
import com.example.docgen.entities.User;
import com.example.docgen.entities.enums.UserRole;
import com.example.docgen.jwt.JwtService;
import com.example.docgen.repositories.UserRepository;
import com.example.docgen.services.CpfValidationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
    private final CpfValidationService cpfValidationService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, CpfValidationService cpfValidationService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.cpfValidationService = cpfValidationService;
        this.passwordEncoder = passwordEncoder;
    }

    // Endpoint para registrar usuário
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRequestDTO data) {
        if (this.userRepository.findByEmail(data.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UserProfileResponseDTO("Este e-mailjá está em uso."));
        }
        if (!cpfValidationService.isValid(data.getCpf())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UserProfileResponseDTO("O CPF informado é inválido."));
        }

        // Cria a entidade User com os dados do DTO
        User newUser = new User();
        newUser.setName(data.getName());
        newUser.setEmail(data.getEmail());
        newUser.setCpf(data.getCpf());
        newUser.setPhone(data.getPhone());
        newUser.setBirthDate(data.getBirthDate());

        // Criptografa a senha antes de salvar
        newUser.setPassword(passwordEncoder.encode(data.getPassword()));

        // Define a role padrão para o usuário
        newUser.setRole(UserRole.USER);

        // Salva a entidade no banco de dados
        userRepository.save(newUser);

        // Retorna uma resposta de sucesso para o frontend
        return ResponseEntity.ok(new UserProfileResponseDTO("Cadastro realizado com sucesso!"));

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


    // Endpoint para editar o nome e telefone

    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateDTO updatedData, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UserProfileResponseDTO("Não autenticado."));
        }

        // Encontra o usuário no banco de dados
        return userRepository.findByEmail(userDetails.getUsername())
                .map(user -> {
                    boolean changed = false;

                    // Atualiza os dados do usuário, permitindo campos nulos
                    if (updatedData.getName() != null && !updatedData.getName().isEmpty() && !updatedData.getName().equals(user.getName())) {
                        user.setName(updatedData.getName());
                        changed = true;
                    }
                    if (updatedData.getPhone() != null && !updatedData.getPhone().isEmpty() && !updatedData.getPhone().equals(user.getPhone())) {
                        user.setPhone(updatedData.getPhone());
                        changed = true;
                    }
                    if (!changed) {
                        return ResponseEntity.ok(new UserProfileResponseDTO("Nenhum dado foi alterado."));
                    }
                    // Salva o usuário atualizado no banco de dados
                    userRepository.save(user);

                    // Retorna os dados do perfil atualizado
                    return ResponseEntity.ok(new UserProfileResponseDTO(user));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new UserProfileResponseDTO("Usuário não encontrado.")));
    }

    // Endpoint para mudar e-mail

    // Endpoint para mudar senha

}


