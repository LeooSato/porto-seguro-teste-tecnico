# Multi-stage build for production image
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests clean package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/portoseguros-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENV SERVER_PORT=8080
ENTRYPOINT ["java","-jar","/app/app.jar"]

