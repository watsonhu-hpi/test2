# Build stage
FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline
COPY src ./src
# Build the application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-slim
WORKDIR /app
# Copy the built JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar
# Create directories for file uploads
RUN mkdir -p uploads/profile-pictures uploads/attachments

# Set the entry point to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]