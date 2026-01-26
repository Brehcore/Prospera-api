# Prospera-api Platform - Backend Service

> API robusta e modular para uma plataforma de e-learning corporativo e gestão de documentos. Construída com Java e
> Spring Boot, a aplicação oferece um sistema completo de autenticação, gestão de usuários e organizações, um módulo
> versátil de cursos e integrações com serviços externos.

O backend é projetado com uma arquitetura de microsserviços, separando responsabilidades em domínios claros como
autenticação, empresa, cursos e integrações.

## ✨ Funcionalidades

O sistema possui um controle de acesso granular baseado em três papéis principais: **Usuário Padrão**, **Administrador
de Organização** e **Administrador do Sistema**.

### 👤 Para Usuários Autenticados (`USER`)

* **Autenticação e Perfil**:
  * Registro de identidade com e-mail e senha.
  * Login seguro com autenticação baseada em JSON Web Tokens (JWT).
  * **Gestão de Segurança**: Alteração de senha e fluxo seguro de alteração de e-mail com verificação em duas etapas (
    código por e-mail).
  * Criação e gerenciamento de perfil de Pessoa Física (PF).
  * Visualização segura do próprio perfil com mascaramento de dados sensíveis (CPF, e-mail, etc.).
  * Solicitação de anonimização e desativação da própria conta.
* **Organizações**:
  * Criação de uma nova Organização (Pessoa Jurídica), tornando-se seu primeiro administrador.
  * Visualização das organizações das quais é membro.
* **Catálogo de Cursos**:
  * Acesso ao catálogo de treinamentos personalizado, baseado nos setores aos quais pertence.
  * Matrícula em treinamentos disponíveis.
  * **Player de Estudo**: Visualização da estrutura completa do curso (módulos e aulas) e reprodução de vídeos.
  * Acompanhamento de progresso em cursos e e-books.
  * Acesso seguro ao conteúdo dos treinamentos (streaming de e-books e imagens).

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
  * Ativar, desativar e resetar a senha de qualquer usuário (admin reset).
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
2. **Configure seu aplication.properties**
3. Execute a aplicação


## 🔮 Futuro e Próximos Passos

Os seguintes módulos continuam em planejamento para desenvolvimento futuro:

- Módulo de Relatórios (Analytics): Para extrair métricas de uso, progresso de membros e engajamento com os
  treinamentos.
- Integração de Pagamentos: Gateways para processamento de assinaturas dos planos já listados.

## 👩🏻‍💻 Autora:

Desenvolvido por Brena Soares