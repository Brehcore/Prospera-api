-- =================================================================================
-- FLYWAY V1: CRIAÇÃO DO SCHEMA INICIAL E INSERÇÃO DO ADMINISTRADOR DO SISTEMA
-- Banco de dados: prospera_db
-- Dialeto: MySQL 8+
-- =================================================================================

-- 1. CRIAÇÃO DAS TABELAS

CREATE TABLE `accounts`
(
    `id`   binary(16) NOT NULL,
    `name` varchar(255) NOT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `auth_users`
(
    `id`                  binary(16) NOT NULL,
    `email`               varchar(255) NOT NULL,
    `enabled`             bit(1)       NOT NULL,
    `password`            varchar(255) NOT NULL,
    `role`                enum('SYSTEM_ADMIN','USER') NOT NULL,
    `personal_account_id` binary(16) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_auth_users_email` (`email`),
    UNIQUE KEY `UK_auth_users_personal_account` (`personal_account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `auth_user_profile_pf`
(
    `id`         binary(16) NOT NULL,
    `birth_date` date         DEFAULT NULL,
    `cpf`        varchar(11)  NOT NULL,
    `full_name`  varchar(255) NOT NULL,
    `phone`      varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_profile_pf_cpf` (`cpf`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `certificates`
(
    `id`                binary(16) NOT NULL,
    `file_path`         varchar(255) NOT NULL,
    `issued_at`         datetime(6) NOT NULL,
    `validation_code`   varchar(255) NOT NULL,
    `enrollment_id`     binary(16) NOT NULL,
    `workload_snapshot` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_certificates_validation_code` (`validation_code`),
    UNIQUE KEY `UK_certificates_enrollment` (`enrollment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `organizations`
(
    `id`           binary(16) NOT NULL,
    `cnpj`         varchar(14)  NOT NULL,
    `razao_social` varchar(255) NOT NULL,
    `status`       enum('ACTIVE','INACTIVE','SUSPENDED') NOT NULL,
    `account_id`   binary(16) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_organizations_cnpj` (`cnpj`),
    KEY            `FK_organizations_account` (`account_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `trainings`
(
    `entity_type`      varchar(31)  NOT NULL,
    `id`               binary(16) NOT NULL,
    `author`           varchar(255) NOT NULL,
    `cover_image_url`  varchar(255) DEFAULT NULL,
    `created_at`       datetime(6) NOT NULL,
    `description`      text         NOT NULL,
    `organization_id`  binary(16) DEFAULT NULL,
    `status`           enum('ARCHIVED','DRAFT','PUBLISHED') DEFAULT NULL,
    `title`            varchar(255) NOT NULL,
    `updated_at`       datetime(6) NOT NULL,
    `file_path`        varchar(255) DEFAULT NULL,
    `file_uploaded_at` datetime(6) DEFAULT NULL,
    `total_pages`      int(11) DEFAULT NULL,
    `meeting_url`      varchar(255) DEFAULT NULL,
    `start_date_time`  datetime(6) DEFAULT NULL,
    `page_count`       int(11) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `course_enrollments`
(
    `id`              binary(16) NOT NULL,
    `completed_at`    datetime(6) DEFAULT NULL,
    `enrolled_at`     datetime(6) NOT NULL,
    `status`          enum('ACTIVE','CANCELLED','COMPLETED','NOT_ENROLLED') NOT NULL,
    `organization_id` binary(16) DEFAULT NULL,
    `training_id`     binary(16) NOT NULL,
    `auth_user_id`    binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY               `FK_enrollments_organization` (`organization_id`),
    KEY               `FK_enrollments_training` (`training_id`),
    KEY               `FK_enrollments_user` (`auth_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `training_modules`
(
    `id`           binary(16) NOT NULL,
    `created_at`   datetime(6) NOT NULL,
    `module_order` int(11) NOT NULL,
    `title`        varchar(255) NOT NULL,
    `updated_at`   datetime(6) NOT NULL,
    `training_id`  binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY            `FK_modules_training` (`training_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `course_lessons`
(
    `id`                  binary(16) NOT NULL,
    `content`             text         DEFAULT NULL,
    `created_at`          datetime(6) NOT NULL,
    `lesson_order`        int(11) NOT NULL,
    `title`               varchar(255) NOT NULL,
    `updated_at`          datetime(6) NOT NULL,
    `module_id`           binary(16) NOT NULL,
    `video_url`           varchar(512) DEFAULT NULL,
    `duration_in_minutes` int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY                   `FK_lessons_module` (`module_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `ebook_progress`
(
    `id`             binary(16) NOT NULL,
    `last_page_read` int(11) NOT NULL,
    `updated_at`     datetime(6) NOT NULL,
    `user_id`        binary(16) NOT NULL,
    `training_id`    binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY              `FK_ebook_progress_training` (`training_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `email_update_tokens`
(
    `id`                binary(16) NOT NULL,
    `expiry_date`       datetime(6) NOT NULL,
    `new_pending_email` varchar(255) NOT NULL,
    `verification_code` varchar(255) NOT NULL,
    `user_id`           binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_email_tokens_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `lesson_progress`
(
    `id`            binary(16) NOT NULL,
    `completed_at`  datetime(6) NOT NULL,
    `enrollment_id` binary(16) NOT NULL,
    `lesson_id`     binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY             `FK_lesson_progress_enrollment` (`enrollment_id`),
    KEY             `FK_lesson_progress_lesson` (`lesson_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `memberships`
(
    `id`                binary(16) NOT NULL,
    `created_at`        datetime(6) NOT NULL,
    `role`              enum('ORG_ADMIN','ORG_MEMBER') NOT NULL,
    `added_by_admin_id` binary(16) DEFAULT NULL,
    `organization_id`   binary(16) NOT NULL,
    `auth_user_id`      binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY                 `FK_memberships_added_by` (`added_by_admin_id`),
    KEY                 `FK_memberships_organization` (`organization_id`),
    KEY                 `FK_memberships_user` (`auth_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `sectors`
(
    `id`   binary(16) NOT NULL,
    `name` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_sectors_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `organization_sectors`
(
    `id`              binary(16) NOT NULL,
    `sector_id`       binary(16) NOT NULL,
    `organization_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_org_sectors` (`organization_id`,`sector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `password_reset_tokens`
(
    `id`          binary(16) NOT NULL,
    `expiry_date` datetime(6) NOT NULL,
    `token`       varchar(255) NOT NULL,
    `user_id`     binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_password_tokens_token` (`token`),
    UNIQUE KEY `UK_password_tokens_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `plans`
(
    `id`               binary(16) NOT NULL,
    `current_price`    decimal(38, 2) NOT NULL,
    `description`      varchar(512)   NOT NULL,
    `duration_in_days` int(11) NOT NULL,
    `is_active`        bit(1)         NOT NULL,
    `name`             varchar(255)   NOT NULL,
    `original_price`   decimal(38, 2) NOT NULL,
    `type`             enum('ENTERPRISE','INDIVIDUAL') NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_plans_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `plan_trainings`
(
    `plan_id`     binary(16) NOT NULL,
    `training_id` binary(16) NOT NULL,
    PRIMARY KEY (`plan_id`, `training_id`),
    KEY           `FK_plan_trainings_training` (`training_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `subscriptions`
(
    `id`         binary(16) NOT NULL,
    `end_date`   datetime(6) NOT NULL,
    `origin`     enum('MANUAL','PAYMENT_GATEWAY') NOT NULL,
    `start_date` datetime(6) NOT NULL,
    `status`     enum('ACTIVE','CANCELED','EXPIRED') NOT NULL,
    `account_id` binary(16) NOT NULL,
    `plan_id`    binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    KEY          `FK_subscriptions_account` (`account_id`),
    KEY          `FK_subscriptions_plan` (`plan_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `training_ratings`
(
    `id`            binary(16) NOT NULL,
    `comment`       varchar(500) DEFAULT NULL,
    `rated_at`      datetime(6) NOT NULL,
    `score`         int(11) NOT NULL,
    `enrollment_id` binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_training_ratings_enrollment` (`enrollment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `training_sector_assignments`
(
    `id`            binary(16) NOT NULL,
    `legal_basis`   varchar(255) DEFAULT NULL,
    `sector_id`     binary(16) NOT NULL,
    `training_id`   binary(16) NOT NULL,
    `training_type` enum('COMPULSORY','ELECTIVE') NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_training_sector_assignments` (`training_id`,`sector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `user_sectors`
(
    `id`              binary(16) NOT NULL,
    `organization_id` binary(16) DEFAULT NULL,
    `sector_id`       binary(16) NOT NULL,
    `user_id`         binary(16) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_user_sectors` (`user_id`,`sector_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;


-- 2. ADIÇÃO DE CONSTRAINTS (FOREIGN KEYS)

ALTER TABLE `auth_users`
    ADD CONSTRAINT `FK_auth_users_account` FOREIGN KEY (`personal_account_id`) REFERENCES `accounts` (`id`);

ALTER TABLE `auth_user_profile_pf`
    ADD CONSTRAINT `FK_profile_pf_user` FOREIGN KEY (`id`) REFERENCES `auth_users` (`id`);

ALTER TABLE `certificates`
    ADD CONSTRAINT `FK_certificates_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `course_enrollments` (`id`);

ALTER TABLE `course_enrollments`
    ADD CONSTRAINT `FK_enrollments_organization` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`),
  ADD CONSTRAINT `FK_enrollments_user` FOREIGN KEY (`auth_user_id`) REFERENCES `auth_users` (`id`),
  ADD CONSTRAINT `FK_enrollments_training` FOREIGN KEY (`training_id`) REFERENCES `trainings` (`id`);

ALTER TABLE `course_lessons`
    ADD CONSTRAINT `FK_lessons_module` FOREIGN KEY (`module_id`) REFERENCES `training_modules` (`id`);

ALTER TABLE `ebook_progress`
    ADD CONSTRAINT `FK_ebook_progress_training` FOREIGN KEY (`training_id`) REFERENCES `trainings` (`id`);

ALTER TABLE `email_update_tokens`
    ADD CONSTRAINT `FK_email_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `auth_users` (`id`);

ALTER TABLE `lesson_progress`
    ADD CONSTRAINT `FK_lesson_progress_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `course_enrollments` (`id`),
  ADD CONSTRAINT `FK_lesson_progress_lesson` FOREIGN KEY (`lesson_id`) REFERENCES `course_lessons` (`id`);

ALTER TABLE `memberships`
    ADD CONSTRAINT `FK_memberships_user` FOREIGN KEY (`auth_user_id`) REFERENCES `auth_users` (`id`),
  ADD CONSTRAINT `FK_memberships_organization` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`),
  ADD CONSTRAINT `FK_memberships_added_by` FOREIGN KEY (`added_by_admin_id`) REFERENCES `auth_users` (`id`);

ALTER TABLE `organizations`
    ADD CONSTRAINT `FK_organizations_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`);

ALTER TABLE `organization_sectors`
    ADD CONSTRAINT `FK_org_sectors_org` FOREIGN KEY (`organization_id`) REFERENCES `organizations` (`id`);

ALTER TABLE `password_reset_tokens`
    ADD CONSTRAINT `FK_password_tokens_user` FOREIGN KEY (`user_id`) REFERENCES `auth_users` (`id`);

ALTER TABLE `plan_trainings`
    ADD CONSTRAINT `FK_plan_trainings_training` FOREIGN KEY (`training_id`) REFERENCES `trainings` (`id`),
  ADD CONSTRAINT `FK_plan_trainings_plan` FOREIGN KEY (`plan_id`) REFERENCES `plans` (`id`);

ALTER TABLE `subscriptions`
    ADD CONSTRAINT `FK_subscriptions_account` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
  ADD CONSTRAINT `FK_subscriptions_plan` FOREIGN KEY (`plan_id`) REFERENCES `plans` (`id`);

ALTER TABLE `training_modules`
    ADD CONSTRAINT `FK_modules_training` FOREIGN KEY (`training_id`) REFERENCES `trainings` (`id`);

ALTER TABLE `training_ratings`
    ADD CONSTRAINT `FK_training_ratings_enrollment` FOREIGN KEY (`enrollment_id`) REFERENCES `course_enrollments` (`id`);


-- 3. INSERÇÃO DO USUÁRIO SYSTEM_ADMIN

-- Variável temporária para armazenar o UUID gerado dinamicamente no formato binário que o JPA utiliza
SET
@admin_uuid = UUID_TO_BIN(UUID());

INSERT INTO `auth_users` (`id`, `email`, `enabled`, `password`, `role`, `personal_account_id`)
VALUES (@admin_uuid,
        'ti@gotreeconsultoria.com.br',
        b'1',
        '$2a$10$EblZqNptyYvcLm/VwDCVAuBjzPKnV702kZqJ9fGv.zX004v0n9KjC',
        'SYSTEM_ADMIN',
        NULL);

-- Inserindo um profile para evitar falhas ao consultar os dados do admin
INSERT INTO `auth_user_profile_pf` (`id`, `cpf`, `full_name`)
VALUES (@admin_uuid,
        '00000000000',
        'Administrador Go-Tree');