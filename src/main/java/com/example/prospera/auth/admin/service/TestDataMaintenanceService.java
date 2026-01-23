package com.example.prospera.auth.admin.service;

import com.example.prospera.auth.repositories.AuthUserRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("local") // O serviço só existe no perfil 'dev'
public class TestDataMaintenanceService {

    private final AuthUserRepository authUserRepository;
    private final JdbcTemplate jdbcTemplate;

    public TestDataMaintenanceService(AuthUserRepository authUserRepository, JdbcTemplate jdbcTemplate) {
        this.authUserRepository = authUserRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void deleteAllUsers() {
        // Checagem de segurança no nome do banco de dados
        String currentDbName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (!"prospera_db".equalsIgnoreCase(currentDbName)) {
            throw new SecurityException("A limpeza de dados só é permitida no banco 'docgenDev'.");
        }

        // Importante: Antes de apagar os usuários (AuthUser), você precisa apagar os perfis
        // que dependem deles para evitar erros de chave estrangeira.
        // O ideal é usar SQL puro para desativar as chaves, truncar e reativar.
        // Exemplo simples (pode precisar de ajuste para seu DB):
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
        jdbcTemplate.execute("TRUNCATE TABLE tb_user_profile_pf;");
        jdbcTemplate.execute("TRUNCATE TABLE tb_user_profile_pj;");
        jdbcTemplate.execute("TRUNCATE TABLE auth_users;");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
	}
}