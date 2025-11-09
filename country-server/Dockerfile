# Use the official Eclipse Temurin base image for building the application
FROM eclipse-temurin:25-jdk-jammy as builder

# Set the working directory
WORKDIR /app

# Copy the Maven wrapper and the POM file
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download the dependencies
RUN ./mvnw dependency:go-offline

# Copy the source code
COPY src src

# Build the application
RUN ./mvnw clean install -DskipTests

# Use a smaller base image for the final image
FROM eclipse-temurin:25-jre-jammy

# Install grpc_health_probe
RUN apt-get update && apt-get install -y curl && \
    curl -L https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/v0.4.24/grpc_health_probe-linux-amd64 -o /usr/local/bin/grpc_health_probe && \
    chmod +x /usr/local/bin/grpc_health_probe

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 9090

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
  CMD /usr/local/bin/grpc_health_probe -addr=:9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
