# Relatorio Tecnico da Aplicacao `portoseguros`

## 1) Escopo e metodo
Este relatorio consolida o estado atual da aplicacao com base no codigo-fonte e configuracoes presentes no workspace.

Fontes principais revisadas:
- `pom.xml`
- `src/main/resources/application.properties`
- `src/main/resources/application-dev.properties`
- `src/main/java/com/testetecnico/portoseguros/**`
- `src/test/java/com/testetecnico/portoseguros/PortosegurosApplicationTests.java`
- logs de execucao compartilhados na conversa (falha de Swagger/OpenAPI)

## 2) Visao geral da solucao
A aplicacao e uma API REST em Spring Boot para um contexto LMS (cadastro de alunos/usuarios, cursos, matriculas e logs de tarefas), com autenticacao JWT e autorizacao por papel.

Stack principal:
- Java 21
- Spring Boot 3.4.2
- Spring Web, Spring Data JPA, Spring Security, Validation
- PostgreSQL (dev) e H2 (runtime disponivel)
- JWT (`jjwt`)
- OpenAPI/Swagger (`springdoc-openapi-starter-webmvc-ui`)
- Lombok

## 3) Arquitetura
Arquitetura em camadas, organizada por pacote:
- `controller`: exposicao HTTP
- `service` e `service.impl`: regras de negocio
- `repository`: acesso a dados via JPA
- `entity`: modelo de dominio
- `dto`: contratos de entrada/saida (records)
- `security`: filtro JWT e provedor de token
- `config`: configuracoes de seguranca, MVC e seeding
- `exception`: excecoes de negocio com `@ResponseStatus`

Ponto positivo:
- Separacao de responsabilidades clara e consistente com padrao em camadas.

Pontos de atencao:
- Ainda existe classe legada `AuthService` marcada como `@Deprecated` em `security/service`.
- Nao ha `@ControllerAdvice` global para padronizacao de erro.

## 4) Modelo de dominio e dados
Entidades principais:
- `Student` (`students`): id UUID, nome, data de nascimento, email unico, telefone, senha, `Role`.
- `Course` (`courses`): id UUID, nome unico, descricao.
- `Enrollment` (`enrollments`): id UUID, relacao aluno-curso, data de matricula e previsao de conclusao.
- `TaskLog` (`task_logs`): id UUID, vinculado a matricula, data, categoria, descricao, tempo em minutos.

Enums:
- `Role`: `ADMIN`, `STUDENT`
- `TaskCategory`: `PESQUISA`, `PRATICA`, `ASSISTIR_VIDEOAULA`

Constraints observadas:
- Unicidade de email em `students`.
- Unicidade de nome em `courses`.
- Unicidade composta (`student_id`, `course_id`) em `enrollments`.

Observacao de migracao:
- O projeto usa `spring.jpa.hibernate.ddl-auto=update`.
- Sem ferramenta de migracao versionada (ex.: Flyway/Liquibase).
- `src/main/resources/db/schema.sql` esta vazio.

## 5) Regras de negocio implementadas
### 5.1 Usuarios/Autenticacao
- Cadastro de usuario/aluno com idade minima de 16 anos.
- Email obrigatoriamente unico.
- Senha armazenada com BCrypt.
- Login por email/senha retorna JWT.
- Criacao de admin existe no service e via seeder (`AdminSeeder`).

### 5.2 Cursos
- CRUD de curso.
- Nome de curso unico (validacao no service + constraint no banco).

### 5.3 Matriculas
- Maximo de 3 matriculas por aluno (`countByStudentId`).
- Impede matricula duplicada no mesmo curso.
- `expectedCompletionDate` = `enrollmentDate + 6 meses`.

### 5.4 Logs de tarefa
- CRUD de log por aluno autenticado.
- Tempo em minutos obrigatoriamente positivo e multiplo de 30.
- Filtro opcional por intervalo de datas.
- Garantia de ownership: aluno so acessa logs de suas matriculas.

## 6) API e endpoints
Controladores detectados:
- `AuthController` (`/auth`)
- `StudentController` (`/students`)
- `CourseController` (`/courses`)
- `EnrollmentController` (`/enrollments`)
- `TaskLogController` (`/tasks`)

Resumo de endpoints:
- `POST /auth/login` -> login JWT
- `POST /students` -> cadastro publico de aluno
- `GET/POST/PUT/DELETE /courses` -> restrito a ADMIN
- `POST /enrollments` e `GET /enrollments/my` -> STUDENT
- `POST/PUT/DELETE/GET /tasks` -> STUDENT

## 7) Seguranca
Configuracao atual em `SecurityConfig`:
- Stateless session
- CSRF desabilitado
- `POST /students` e `/auth/**` permitidos sem autenticacao
- `/swagger-ui/**` e `/v3/api-docs/**` permitidos
- `/courses/**` exige role ADMIN
- Demais rotas autenticadas

Fluxo JWT:
- Token gerado em `JwtTokenProvider` com claims: `sub`(id), `email`, `role`, `name`.
- `JwtAuthenticationFilter` extrai `Bearer`, valida token e injeta `Authentication` com `ROLE_<role>`.
- Token invalido/malformado e ignorado no filtro sem quebrar request.

Riscos/observacoes:
- Segredo JWT com valor default hardcoded (fallback em property).
- `AdminSeeder` usa credenciais fixas (`admin@lms.com` / senha `123`) no codigo.
- Log mostra `UserDetailsServiceAutoConfiguration` gerando senha padrao, indicando configuracao default adicional ativa.

## 8) OpenAPI/Swagger
Configuracoes vistas:
- `application.properties`:
  - `springdoc.api-docs.enabled=true`
  - `springdoc.swagger-ui.enabled=true`
- `pom.xml` com `springdoc-openapi-starter-webmvc-ui` em `2.8.7`.

Historico de erro observado nos logs:
- Falha de startup com pattern invalido:
  - `/swagger-ui/**/*swagger-initializer.js`
- Mensagem indica conflito de parser de path pattern.

Status de configuracao no codigo atual:
- `WebConfig` esta neutro (sem override de parser).
- `application-dev.properties` anexado nao contem mais override legado de path matcher.

## 9) Qualidade e testes
Estado atual de testes:
- Apenas `contextLoads()` em `PortosegurosApplicationTests`.

Lacunas relevantes:
- Sem testes unitarios de services (regras de negocio criticas).
- Sem testes de integracao de seguranca/autorizacao.
- Sem testes de contrato para endpoints.

## 10) Riscos tecnicos prioritarios
### Alta prioridade
1. Instabilidade de startup relacionada a Swagger/OpenAPI em ambiente de execucao (ja observada nos logs).
2. Credenciais e segredo sensiveis no codigo/config default (`AdminSeeder`, fallback JWT secret).

### Media prioridade
1. Ausencia de migracoes versionadas de banco.
2. Ausencia de tratamento global e padronizado de erro (`@ControllerAdvice`).
3. Cobertura de testes muito baixa para regras de negocio e seguranca.

### Baixa prioridade
1. Classe legada `AuthService` ainda registrada como `@Service`.
2. Parametros de banco redundantes (`database-platform` e `hibernate.dialect`).

## 11) Recomendacoes praticas
1. Stabilizar Swagger definitivamente:
   - Garantir limpeza de cache/build (`mvn clean`) e alinhamento de versoes efetivamente usadas em runtime.
   - Validar startup com profile `dev` e acessar `/swagger-ui/index.html`.
2. Endurecer seguranca:
   - Externalizar `security.jwt.secret` por variavel de ambiente.
   - Remover senha fraca do seeder; usar bootstrap seguro por variaveis.
3. Governanca de banco:
   - Introduzir Flyway/Liquibase e migracoes para `students.role`, `enrollments`, `task_logs`.
4. Observabilidade de API:
   - Adicionar `@ControllerAdvice` com payload padrao de erro.
5. Testes:
   - Unitarios para `UserServiceImpl`, `EnrollmentServiceImpl`, `TaskLogServiceImpl`.
   - Integracao para autorizacao (ADMIN vs STUDENT) e ownership.

## 12) Conclusao
A aplicacao ja possui uma base funcional boa para um LMS com autenticacao JWT e controle por roles, incluindo regras de matricula e task logging.

O maior ganho imediato esta em:
- consolidar startup Swagger/OpenAPI em todos os ambientes,
- remover hardcoded secrets/credenciais,
- elevar cobertura de testes e governanca de banco.

Com esses ajustes, o projeto sobe de um estado funcional para um nivel mais confiavel para evolucao continua e uso em producao controlada.

