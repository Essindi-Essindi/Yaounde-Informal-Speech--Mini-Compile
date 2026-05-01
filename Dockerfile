# ── Stage 1: Build ──────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first (layer-cache: only re-downloads deps if pom changes)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build the fat jar
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the fat jar from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT at runtime (default 10000).
# Spring Boot will bind to it via SERVER_PORT env var.
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
