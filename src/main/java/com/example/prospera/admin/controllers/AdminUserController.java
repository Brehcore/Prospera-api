package com.example.prospera.admin.controllers;

import com.example.prospera.admin.dto.AdminUserDetailDTO;
import com.example.prospera.admin.dto.AdminUserSummaryDTO;
import com.example.prospera.admin.dto.AdminUserUpdateRequest;
import com.example.prospera.admin.services.AdminUserService;
import com.example.prospera.auth.domain.AuthUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Controller responsável pela gestão de usuários administrativos do sistema.
 * Oferece endpoints para consulta, atualização e gerenciamento de perfis
 * administrativos, incluindo ativação e desativação de contas.
 * <p>
 * É necessário que o usuário autenticado possua credenciais autorizadas com
 * o papel `SYSTEM_ADMIN` para acessar qualquer operação fornecida por esta classe.
 */
@RestController
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Recupera uma lista paginada de usuários administrativos do sistema.
     * O retorno pode ser filtrado com base em um endereço de email específico, 
     * se o parâmetro opcional `email` for fornecido.
     *
     * @param email (opcional) Filtro opcional para buscar usuários cujo email contenha a string especificada.
     * @param pageable Configuração de paginação e ordenação, com tamanho padrão de página = 10 e ordenação por email.
     * @return ResponseEntity contendo um objeto {@link Page} de {@link AdminUserSummaryDTO} com informações
     * resumidas dos usuários e o status HTTP 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Page<AdminUserSummaryDTO>> getAllUsers(
            @RequestParam(required = false) String email,
            @PageableDefault(size = 10, sort = "email") Pageable pageable) {

        // O map do Page do Spring converte automaticamente o conteúdo da página!
        Page<AdminUserSummaryDTO> usersPage = adminUserService.getAllUsers(email, pageable)
                .map(AdminUserSummaryDTO::fromEntity);

        return ResponseEntity.ok(usersPage);
    }

    /**
     * Edita e atualiza as informações de um usuário administrativo existente.
     * Realiza a validação das informações recebidas no payload antes de efetuar a persistência.
     *
     * @param userId  Identificador único do usuário que será atualizado.
     * @param request Dados atualizados do usuário enviados no corpo da requisição.
     * @return ResponseEntity contendo um objeto {@link AdminUserDetailDTO} com os detalhes atualizados
     * do usuário e o status HTTP 200 (OK).
     */
    @PutMapping("/{userId}")
    public ResponseEntity<AdminUserDetailDTO> updateUser(
            @PathVariable UUID userId,
            @RequestBody @Valid AdminUserUpdateRequest request) {

        AuthUser updatedUser = adminUserService.updateUser(userId, request);
        return ResponseEntity.ok(AdminUserDetailDTO.fromEntity(updatedUser));
    }

    /**
     * Recupera os detalhes completos de um usuário administrativo específico.
     *
     * @param userId Identificador único do usuário.
     * @return ResponseEntity contendo um {@link AdminUserDetailDTO} com os detalhes do usuário
     * e o status HTTP 200 (OK).
     */
    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserDetailDTO> getUserDetails(@PathVariable UUID userId) {
        AdminUserDetailDTO userDetails = AdminUserDetailDTO.fromEntity(
                adminUserService.getUserDetails(userId)
        );
        return ResponseEntity.ok(userDetails);
    }

    /**
     * Desativa a conta de um usuário administrativo. Após a desativação,
     * o acesso ao sistema será bloqueado para o usuário.
     *
     * @param userId Identificador único do usuário administrativo que será desativado.
     * @return ResponseEntity contendo uma mensagem confirmando a desativação e 
     * o status HTTP 200 (OK).
     */
    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(@PathVariable UUID userId) {
        adminUserService.deactivateUser(userId);
        return ResponseEntity.ok("Usuário desativado com sucesso.");
    }

    /**
     * Reativa a conta de um usuário administrativo previamente desativado.
     * Após a reativação, o usuário poderá acessar novamente o sistema.
     *
     * @param userId Identificador único do usuário administrativo que será ativado.
     * @return ResponseEntity contendo uma mensagem confirmando a reativação e 
     * o status HTTP 200 (OK).
     */
    @PatchMapping("/{userId}/activate")
    public ResponseEntity<String> activateUser(@PathVariable UUID userId) {
        adminUserService.activateUser(userId);
        return ResponseEntity.ok("Usuário ativado com sucesso.");
    }
}