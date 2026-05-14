# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Security: Non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the JAR
COPY --from=build /app/target/*.jar app.jar

# (Optional) Create a directory for external templates/configs
# This allows mounting external XSDs or Templates later
# VOLUME ["/app/config"]

EXPOSE 9876
EXPOSE 8080

# Optimized for Virtual Threads & Low Latency
ENTRYPOINT ["java", \
            "-XX:+UseZGC", \
            "-XX:+ZGenerational", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", "app.jar"]
