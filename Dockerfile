# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built jar from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Copy templates, XSDs, and Dictionary (Important for your Mapper!)
# These are usually in src/main/resources, which are packed into the JAR,
# but if you want to override them externally, you'd mount them as volumes.

# Expose the Netty TCP Port and Actuator Port
EXPOSE 9876
EXPOSE 8080

# Run the application with optimized Virtual Thread settings
ENTRYPOINT ["java", "-jar", "app.jar"]