# 📝 docgen

Sistema de automação de **relatórios e documentos Word**, com interface web e backend em **Spring Boot**.  
Conta com autenticação, validação de dados (incluindo CPF) e estrutura pronta para expansão.

---

## 🚀 Tecnologias Utilizadas

- ☕ **Java 17+**
- ⚙️ **Spring Boot**
- 🔐 **Spring Security**
- 🛠️ **Spring Data JPA**
- 🗄️ **MySQL** (via XAMPP)
- 📦 **Hibernate Validator**
- 📑 **Stella Bean Validation** (validação de CPF)
- 🖥️ **HTML/CSS/JavaScript** (em breve)

---

## 📚 Funcionalidades

- ✅ **Cadastro de usuários** com:
    - Validação de **e-mail** (único), **CPF** (único), **senha** (mín. 8 caracteres) e **telefone**
    - Verificação automática da **data de nascimento**
    - Cadastro individual e em lote
- ✅ **Sistema de autenticação** com:
    - Autenticação básica via e-mail/senha utilizando `AuthenticationManager`
    - Controle de acesso baseado em roles (**ADMIN** / **USER**)
    - Proteção de endpoints (ex: PUT/DELETE acessíveis apenas por ADMIN)
    - Tratamento de erros e mensagens claras para falhas de autenticação/autorização
- 🔄 **CRUD de usuários** com:
    - Listagem, busca por ID, atualização e deleção de usuários
    - Restrições de permissão aplicadas conforme o papel do usuário
- 📄 **Geração futura de documentos** `.docx` a partir de dados personalizados

---

## 🏁 Como Executar

1. **Clone o projeto:**

   ```bash
   git clone https://github.com/seu-usuario/docgen.git
   cd docgen
   ```

2. **Configure seu banco de dados (XAMPP):**

   - Acesse o **phpMyAdmin** e crie um banco de dados chamado:

     ```
     docgen
     ```

3. **Ajuste as credenciais em `src/main/resources/application.properties`:**

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/docgen
   spring.datasource.username=root
   spring.datasource.password=
   ```

---

## 👨‍💻 Autor

Desenvolvido por **Juan Guilherme Silva Lemos**  
📧 [juangsilvalemos@gmail.com](mailto:juangsilvalemos@gmail.com)  
💙 Conecte-se no [LinkedIn](https://www.linkedin.com/in/juan-guilherme-silva-lemos-40b516244/)
