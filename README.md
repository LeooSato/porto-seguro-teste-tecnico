# Porto Seguro – Teste Técnico (LMS)

API REST em Spring Boot 3.4.2 (Java 21) para gestão de estudantes, cursos, matrículas e registros de tarefas, com autenticação JWT stateless e Postgres.

## Visão geral
- Stack: Spring Boot 3.4.2, Java 21, Maven, Postgres, Flyway, Spring Security + JWT, SpringDoc OpenAPI.
- Camadas: controllers → services → repositories → entities; DTOs como `record` em `dto/`. Entidades usam UUID e Lombok builder.
- Swagger: `/swagger-ui/index.html` (quando a app estiver rodando).
- Produção (Render): https://porto-seguro-teste-tecnico.onrender.com

## Autenticação e autorização
- Login: `POST /auth/login` com `email` e `password` → retorna JWT com claims `sub` (student id), `email`, `role`, `name`.
- Header: `Authorization: Bearer <token>`.
- Roles: `ADMIN` e `STUDENT`.
- Acesso às rotas principais:
  - Público: `POST /auth/**`, `POST /students`, `GET /swagger-ui/**`, `GET /v3/api-docs/**`, `OPTIONS /**`.
  - Cursos: `GET /courses/**` (ADMIN, STUDENT); criar/editar/excluir apenas `ADMIN`.
  - Matrículas: `POST /enrollments`, `GET /enrollments/**` (ADMIN, STUDENT); `POST` usa ID do estudante do token.
  - Tarefas (`/tasks/**`): apenas `STUDENT`.
  - Demais rotas: autenticadas.

## Regras de negócio (essenciais)
- Estudantes: idade mínima 16 anos; email único; senha armazenada com BCrypt.
- Cursos: nome único (case-insensitive) e aparado em criação/atualização.
- Matrículas: máximo 3 cursos por estudante; duplicidade bloqueada; data prevista de conclusão = data de matrícula + 6 meses.
- Task logs: categorias `PESQUISA | PRATICA | ASSISTIR_VIDEOAULA`; tempo >0 e múltiplo de 30 min; intervalo de datas exige `endDate >= startDate`; listagem e mutações sempre respeitam propriedade do estudante.

## Dados iniciais
- Seeder cria (se não existir) admin: `admin@lms.com` / senha `123` (telefone `+55 11 00000-0000`, nascimento `1990-01-01`). Ajuste no seed ou altere via banco após o primeiro login.

## Variáveis de ambiente principais
- `SERVER_PORT` (default 8080)
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATASOURCE_MAX_POOL_SIZE`
- `SECURITY_JWT_SECRET` (≥32 chars ASCII) — usado pelo `JwtTokenProvider`
- `JWT_SECRET` (legado em `application.properties`); mantenha o mesmo valor de `SECURITY_JWT_SECRET` para evitar divergência.
- Perfil dev (`SPRING_PROFILES_ACTIVE=dev`) aponta para banco Neon (ver `src/main/resources/application-dev.properties`).

## Como rodar localmente
1) Pré-requisitos: Java 21 e Docker (opcional para banco). Maven Wrapper já incluso.
2) Suba Postgres via Docker (opcional, usa config do `docker-compose.yml`):
   ```sh
   docker compose up -d db
   ```
3) Execute a aplicação:
   - Windows: `mvnw.cmd spring-boot:run`
   - Linux/macOS: `./mvnw spring-boot:run`
4) Testes: `mvnw.cmd test` ou `./mvnw test`
5) Documentação: acesse `http://localhost:8080/swagger-ui/index.html`.

### Executar tudo via Docker Compose
```sh
docker compose up -d
```
O serviço `app` sobe com perfil `dev` e conecta no Postgres do próprio compose (porta 8080 exposta).

## Banco e migrações
- Flyway habilitado; migrações em `src/main/resources/db/migration`.
- JPA `ddl-auto=none` (schema controlado por Flyway).
- Estratégia de nomenclatura camelCase → snake_case.

## Estrutura do projeto
- `src/main/java/com/testetecnico/portoseguros/config` — segurança (JWT filter/TokenProvider) e seeder.
- `controller` / `service` / `repository` — endpoints, regras e persistência.
- `entity` — modelos de domínio (`Student`, `Course`, `Enrollment`, `TaskLog`, `Role`, `TaskCategory`).
- `dto` — records de request/response.
- `docs/` — relatórios e notas de implantação (Render/Neon).

## Observabilidade e saúde
- Actuator: `/actuator/health`, `/actuator/info` expostos.

## Produção
- Render: https://porto-seguro-teste-tecnico.onrender.com
- Swagger em produção: `/swagger-ui/index.html` (quando deploy estiver ativo).

## Dicas rápidas de uso
1) Crie estudante: `POST /students`.
2) Faça login: `POST /auth/login` → obtenha o token.
3) Cadastre cursos (ADMIN) e matricule estudantes: `POST /enrollments`.
4) Registre atividades: `POST /tasks` (STUDENT).
