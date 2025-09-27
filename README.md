# DocGen Platform - Backend Service

> API robusta e modular para uma plataforma de e-learning corporativo e gestão de documentos. Construída com Java e
Spring Boot, a aplicação oferece um sistema completo de autenticação, gestão de usuários e organizações, um módulo
versátil de cursos e integrações com serviços externos.

O backend é projetado com uma arquitetura de microsserviços, separando responsabilidades em domínios claros como
autenticação, empresa, cursos e integrações.

## ✨ Funcionalidades

O sistema possui um controle de acesso granular baseado em três papéis principais: **Usuário Padrão**, **Administrador
de Organização** e **Administrador do Sistema**.

### 👤 Para Usuários Autenticados (`USER`)

* **Autenticação e Perfil**:
  * Registro de identidade com e-mail e senha.
  * Login seguro com autenticação baseada em JSON Web Tokens (JWT).
  * Criação e gerenciamento de perfil de Pessoa Física (PF).
  * Visualização segura do próprio perfil com mascaramento de dados sensíveis (CPF, e-mail, etc.).
  * Solicitação de anonimização e desativação da própria conta.
* **Organizações**:
  * Criação de uma nova Organização (Pessoa Jurídica), tornando-se seu primeiro administrador.
  * Visualização das organizações das quais é membro.
* **Catálogo de Cursos**:
  * Acesso ao catálogo de treinamentos personalizado, baseado nos setores aos quais pertence.
  * Matrícula em treinamentos disponíveis.
  * Acompanhamento de progresso em cursos e e-books.
  * Acesso seguro ao conteúdo dos treinamentos (streaming de e-books).

### 🏢 Para Administradores de Organização (`ORG_ADMIN`)

* **Gestão de Membros**:
  * Convidar e adicionar novos membros à sua organização por e-mail.
  * Atribuir papéis de "Administrador" ou "Membro" a usuários dentro da organização.
  * Remover membros da organização.
  * Visualizar uma lista detalhada de todos os membros, incluindo quem os adicionou e seus setores.
* **Gestão de Setores**:
  * "Adotar" setores do catálogo global para uso interno da organização.
  * Atribuir membros a um ou mais setores.
* **Gestão de Treinamentos**:
  * Matricular múltiplos membros da organização em um treinamento de uma só vez (matrícula em massa).
  * Visualizar o progresso de um membro específico nos treinamentos em que ele está matriculado.
  * Visualizar quais membros estão matriculados em um treinamento específico.

### ⚙️ Para Administradores do Sistema (`SYSTEM_ADMIN`)

* **Gestão Global de Usuários**:
  * Listar todos os usuários do sistema, com filtros de busca.
  * Ativar, desativar e resetar a senha de qualquer usuário.
* **Gestão Global de Organizações**:
  * Listar todas as organizações da plataforma.
  * Visualizar detalhes de qualquer organização, incluindo sua lista de membros.
  * Alterar o status de uma organização (Ativa, Inativa, etc.).
* **Gestão do Catálogo de Conteúdo**:
  * CRUD completo de Treinamentos: criar, listar, detalhar, atualizar e deletar.
  * Gerenciar o ciclo de vida dos treinamentos, alterando seu status para `Rascunho`, `Publicado` ou `Arquivado`.
  * Fazer upload de arquivos de conteúdo (e-books em PDF) e imagens de capa.
  * Construir a estrutura de cursos gravados, adicionando módulos e lições.
* **Gestão de Setores e Integrações**:
  * CRUD completo do catálogo global de setores.
  * Associar treinamentos a setores, definindo-os como **Obrigatórios** ou **Eletivos**.

## 🛠️ Tecnologias Utilizadas

* **Linguagem e Frameworks**:
  * **Java 21**
  * **Spring Boot 3.4.8**
  * **Spring Security**: Para autenticação e autorização.
  * **Spring Data JPA**: Para persistência de dados com o Hibernate.
  * **Spring WebFlux (WebClient)**: Para comunicação reativa com outros microsserviços.
* **Segurança**:
  * **JSON Web Tokens (JWT)**: Para gerenciamento de sessão stateless.
  * **BCrypt**: Para hashing de senhas.
* **Bibliotecas**:
  * **Lombok**: Para redução de código boilerplate.
  * **Caelum Stella**: Para validação de documentos brasileiros (CPF/CNPJ).
  * **Apache PDFBox**: Para processamento de arquivos PDF (extração de número de páginas).
  * **JJwt (Java JWT)**: Para criação e validação dos tokens.
* **Banco de Dados**:
  * MySQL 8.
* **Build Tool**:
  * Maven

## 🏛️ Arquitetura e Decisões de Design

* **Microsserviços**: A aplicação é desenhada como um serviço que se comunica com outros (ex: um serviço "Enterprise")
  via APIs REST. O `WebClient` é configurado para propagar o token JWT de autenticação, garantindo a segurança na
  comunicação entre serviços.
* **Design Modular**: O código é organizado em pacotes que representam domínios de negócio claros (`auth`, `courses`,
  `enterprise`, `integration`), promovendo baixo acoplamento e alta coesão.
* **Segurança com JWT e RBAC Dinâmico**: O sistema utiliza um fluxo de autenticação stateless. Após o login, um JWT
  contendo papéis e permissões é gerado. A entidade `AuthUser` implementa uma lógica de papéis dinâmica: as permissões
  de um usuário podem mudar com base em sua afiliação e papel dentro de uma organização.
* **Modelo de Conteúdo Polimórfico**: O módulo de cursos utiliza uma estratégia de herança JPA `SINGLE_TABLE` para a
  entidade `Training`. Isso permite que diferentes tipos de conteúdo (E-books, Cursos Gravados, Aulas ao Vivo) coexistam
  na mesma tabela, com um DTO polimórfico (`TrainingDetailDTO`) que se adapta para expor os dados específicos de cada
  tipo.
* **Centralização da Lógica de Autorização**: Regras de permissão complexas e reutilizáveis (ex: "verificar se o usuário
  é admin desta organização") são centralizadas no `AuthorizationService` para evitar duplicação de código e garantir
  consistência.
* **Otimização de Performance**: Os repositórios JPA fazem uso extensivo de `JOIN FETCH` em consultas customizadas (
  `@Query`) para carregar entidades relacionadas de forma eficiente, prevenindo o problema de N+1 queries.
* **Tratamento Global de Exceções**: A classe `ResourceExceptionHandler` com a anotação `@RestControllerAdvice` captura
  exceções de toda a aplicação, padronizando as respostas de erro da API para diferentes cenários (ex: validação,
  recurso não encontrado, acesso negado).

## 🚀 Começando

### Pré-requisitos

- JDK 21
- Maven
- Instância de um banco de dados SQL
- Docker ou Xampp (A seu critério)

### Instalação e Execução

1. **Clone o repositório:**
   ```bash
   git clone [URL_DO_REPOSITORIO]
   cd [NOME_DO_PROJETO]
   ```
2. **Configure as variáveis de ambiente:**
   Crie um arquivo `application.properties` em `src/main/resources/` e configure as seguintes propriedades (Lembre-se o
   properties é um arquivo de segurança da sua aplicação, então lembre-se de adicionar ao gitignore):
   ```properties
   # Perfil ativo (lembre-se de criar o perfil e ajustar no seu MaintenanceController
   spring.profiles.active=dev
   
   # Configuração do Banco de Dados
   spring.datasource.url=jdbc:postgresql://localhost:5432/docgen_final
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha

   # Chave secreta para JWT (gere uma chave segura em Base64)
   application.security.jwt.secret-key=sua_chave_secreta_muito_longa_e_segura

   # Expiração do Token (em milissegundos, ex: 24 horas)
   application.security.jwt.expiration=86400000
   
   # O tamanho máximo para upload de arquivos e para requisição total é de 50MB
   spring.servlet.multipart.max-file-size=50MB
   spring.servlet.multipart.max-request-size=50MB

   # URL de outros microsserviços
   enterprise.service.url=http://localhost:8081 # Exemplo
   ```
3. **Execute a aplicação (usando Maven):**
   ```bash
   mvn spring-boot:run
   ```
   A aplicação estará disponível em `http://localhost:8080`.

## ⚙️ Perfis (Profiles) do Spring

* **`dev`**: Ativa endpoints de manutenção (`/admin/maintenance`) que permitem, por exemplo, apagar todos os usuários do
  banco de desenvolvimento.
* **`prod`** (padrão): Desativa funcionalidades perigosas de desenvolvimento.

Para ativar um perfil, adicione ao `application.properties`:
`spring.profiles.active=dev`

## 📖 Endpoints da API

A seguir, a lista dos principais endpoints agrupados por funcionalidade.

🌐 API Pública (Não requer autenticação)

| Método | Rota                                 | Descrição                                                                         |
| :----- | :----------------------------------- | :-------------------------------------------------------------------------------- |
| `POST` | `/auth/register`                     | Registra um novo usuário.                                  |
| `POST` | `/auth/login`                        | Autentica um usuário e retorna um token JWT.               |
| `GET`  | `/public/catalog`                    | Lista todos os treinamentos publicados disponíveis na vitrine. |
| `GET`  | `/public/catalog/{trainingId}`       | Exibe os detalhes públicos de um treinamento específico.       |
| `GET`  | `/public/catalog/sectors`            | Lista todos os setores globais disponíveis para filtro.      |
| `GET`  | `/stream/images/{filename}`          | Serve arquivos de imagem (ex: capas de cursos).               |
| `GET`  | `/api/lookup/cnpj/{cnpj}`            | Consulta dados de um CNPJ em uma API externa (BrasilAPI).         |

👤 API de Usuário Autenticado

| Método | Rota                                       | Descrição                                                                                             |
| :----- | :----------------------------------------- | :---------------------------------------------------------------------------------------------------- |
| `GET`  | `/profile/me`                              | Retorna o perfil completo do usuário logado (com dados mascarados).         |
| `POST` | `/profile/pf`                              | Cria o perfil de Pessoa Física para o usuário logado.                      |
| `GET`  | `/profile/me/organizations`                | Lista as organizações das quais o usuário é membro.                     |
| `POST` | `/organizations`                           | Cria uma nova organização (o criador se torna `ORG_ADMIN`).              |
| `GET`  | `/trainings/catalog`                       | Retorna o catálogo de treinamentos personalizado para o usuário.                 |
| `GET`  | `/trainings/my-enrollments`                | Lista todos os treinamentos em que o usuário está matriculado.             |
| `POST` | `/trainings/{trainingId}/enroll`           | Matricula o usuário em um treinamento.                                         |
| `POST` | `/trainings/lessons/{lessonId}/complete`   | Marca uma lição como concluída.                                                |
| `GET`  | `/progress/ebooks/{trainingId}`            | Retorna o progresso do usuário em um e-book.                                   |
| `PUT`  | `/progress/ebooks/{trainingId}`            | Atualiza o progresso do usuário em um e-book (última página lida).            |
| `GET`  | `/stream/ebooks/{trainingId}`              | Acessa o conteúdo de um e-book (requer matrícula).                           |

🏢 API de Administrador de Organização (`ORG_ADMIN`)

| Método | Rota                                                        | Descrição                                                                                |
| :----- | :---------------------------------------------------------- | :--------------------------------------------------------------------------------------- |
| `POST` | `/organizations/{orgId}/members`                            | Adiciona um novo membro à organização.                             |
| `GET`  | `/organizations/{orgId}/members`                            | Lista todos os membros da organização.                               |
| `DELETE` | `/organizations/{orgId}/members/{membershipId}`             | Remove um membro da organização.                                     |
| `PATCH`  | `/organizations/{orgId}/members/{membershipId}`             | Altera o papel de um membro (`ORG_ADMIN` ou `ORG_MEMBER`).             |
| `GET`  | `/organizations/{orgId}/members/{membershipId}`             | Exibe os detalhes de um membro específico.                         |
| `POST` | `/organizations/{orgId}/members/{membershipId}/sectors`     | Atribui um membro a um setor.                                      |
| `GET`  | `/organizations/{orgId}/sectors`                            | Lista os setores que a organização "adotou".                   |
| `POST` | `/organizations/{orgId}/sectors`                            | "Adota" um setor do catálogo global para a organização.       |
| `DELETE` | `/organizations/{orgId}/sectors/{sectorId}`                 | Remove um setor da organização.                               |
| `POST` | `/organizations/{orgId}/enrollments`                        | Matricula múltiplos membros em um treinamento (matrícula em massa). |
| `GET`  | `/organizations/{orgId}/trainings/{trainingId}/enrollments` | Lista os membros da organização que estão matriculados em um treinamento. |
| `GET`  | `/organizations/{orgId}/assignable-trainings`               | Lista os treinamentos disponíveis para a organização atribuir aos seus membros. |

️API de Administrador do Sistema (`SYSTEM_ADMIN`)

| Método | Rota                                       | Descrição                                                                            |
| :----- | :----------------------------------------- | :----------------------------------------------------------------------------------- |
| `GET`  | `/admin/users`                             | Lista todos os usuários do sistema.                             |
| `PATCH`  | `/admin/users/{userId}/activate`           | Ativa a conta de um usuário.                                      |
| `PATCH`  | `/admin/users/{userId}/deactivate`         | Desativa a conta de um usuário.                                    |
| `GET`  | `/admin/organizations`                     | Lista todas as organizações da plataforma.              |
| `GET`  | `/admin/organizations/{orgId}`             | Exibe detalhes de uma organização específica.                 |
| `PATCH`  | `/admin/organizations/{orgId}/status`      | Altera o status de uma organização.                        |
| `GET`  | `/admin/sectors`                           | Lista todos os setores globais.                                   |
| `POST` | `/admin/sectors`                           | Cria um novo setor global.                                        |
| `DELETE` | `/admin/sectors/{sectorId}`                | Deleta um setor global (se não estiver em uso). |
| `GET`  | `/admin/trainings`                         | Lista todos os treinamentos do sistema.                        |
| `POST` | `/admin/trainings`                         | Cria um novo treinamento.                                     |
| `PUT`  | `/admin/trainings/{trainingId}`            | Atualiza os dados de um treinamento.                           |
| `DELETE` | `/admin/trainings/{trainingId}`            | Deleta um treinamento (se não tiver matrículas/módulos).     |
| `POST` | `/admin/trainings/{trainingId}/publish`    | Publica um treinamento.                                     |
| `POST` | `/admin/trainings/{trainingId}/archive`    | Arquiva um treinamento.                                      |
| `POST` | `/admin/trainings/{trainingId}/sectors`    | Associa um treinamento a um setor (obrigatório/eletivo).       |
| `POST` | `/admin/trainings/{trainingId}/cover-image`| Faz upload da imagem de capa de um treinamento.                  |

## 🔮 Futuro e Próximos Passos

Conforme solicitado, os seguintes módulos ainda estão planejados para desenvolvimento futuro:

* **Módulo de Relatórios (Analytics)**: Para extrair métricas de uso, progresso de membros e engajamento com os
  treinamentos.
* **Módulo de Assinaturas e Pagamentos**: Para implementar planos, assinaturas e integração com gateways de pagamento,
  permitindo a monetização da plataforma.

---

## 👩🏻‍💻 Autor(a)

Desenvolvido por **Brena Soares**

[![LinkedIn](https://img.shields.io/badge/linkedin-%230077B5.svg?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/brenasoares/)
[![GitHub](https://img.shields.io/badge/github-%23121011.svg?style=for-the-badge&logo=github&logoColor=white)](https://github.com/Brehcore)