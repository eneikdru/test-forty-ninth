# Multi-stage Dockerfile for Eneik Epidemiology Knowledge Base Backend

# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B || true

COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
    postgresql-client \
    cron \
    curl \
    ca-certificates \
    && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app.jar
COPY scripts/ /app/scripts/
RUN chmod +x /app/scripts/*.sh || true

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
