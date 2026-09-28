# =============================================================================
# TaskFlow API — Multi-stage Docker build
# Stage 1: compile with Maven  |  Stage 2: slim JRE runtime
# =============================================================================

# ----- Build stage -----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first (faster rebuilds when only source changes)
COPY pom.xml .
RUN mvn -B dependency:go-offline -DskipTests

COPY src ./src
RUN mvn -B package -DskipTests

# ----- Runtime stage -----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user for better security hygiene
RUN groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /app/target/taskflow-api-*.jar /app/app.jar

EXPOSE 8080

# JVM container-aware flags
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
