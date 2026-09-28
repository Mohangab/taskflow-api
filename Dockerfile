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

# curl for HEALTHCHECK; non-root user for better security hygiene
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system app && useradd --system --gid app app
USER app

COPY --from=build /app/target/taskflow-api-*.jar /app/app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -fsS http://127.0.0.1:8080/actuator/health | grep -q UP || exit 1

# JVM container-aware flags
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
