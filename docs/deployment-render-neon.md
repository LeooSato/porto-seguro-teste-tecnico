# Deployment Guide (Render + Neon)

## Prerequisites
- Render account and Docker deploy enabled
- Neon project with a Postgres database
- Environment secrets ready: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, optional `SERVER_PORT`

## Neon Setup
1. Create a new Neon project and database.
2. Copy the connection string and set SSL mode to `require` if needed.
3. Collect credentials for env vars: URL, user, password.

## Render (Docker Deploy)
1. Push the repo to GitHub.
2. In Render, create a **Web Service** from the repo and choose **Docker**.
3. Set build command: (Render uses the Dockerfile automatically; no custom build command needed).
4. Set env vars:
   - `SPRING_DATASOURCE_URL` = `jdbc:postgresql://<neon-host>:5432/<db>?sslmode=require`
   - `SPRING_DATASOURCE_USERNAME` = `<neon user>`
   - `SPRING_DATASOURCE_PASSWORD` = `<neon password>`
   - `JWT_SECRET` = strong random secret
   - `SERVER_PORT` = `10000` (Render injects `PORT`, but we default with fallback; map `PORT` to `SERVER_PORT` in Render if desired)
   - `SPRING_PROFILES_ACTIVE` = `dev` or `prod` as you prefer
5. Deploy; Flyway will run automatically on startup.
6. Health check: `GET https://<service>.onrender.com/actuator/health`.

## Local Docker Compose
```bash
docker-compose up --build
```
- App: http://localhost:8080
- DB: localhost:5432 (`postgres`/`postgres`, db `portoseguros`)

## Notes
- Ensure Neon user has privileges to create tables.
- Flyway migration runs at startup; subsequent deploys apply new migrations automatically.
- Keep JWT secret long and random; rotate when needed.

