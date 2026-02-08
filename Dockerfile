# ======================
# Stage 1: Build
# ======================
FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application
COPY src ./src
RUN mvn clean package -DskipTests

# ======================
# Stage 2: Runtime
# ======================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Non-root user
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Copy jar
COPY --from=build /app/target/*.jar app.jar

EXPOSE 3000

# Optional healthcheck (requires actuator)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:3000/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
