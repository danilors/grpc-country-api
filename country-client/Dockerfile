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

# Set the working directory
WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port the app runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
