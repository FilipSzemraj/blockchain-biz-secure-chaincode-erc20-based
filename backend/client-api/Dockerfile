# Use JDK 21 base image
FROM eclipse-temurin:21-jdk-jammy

# Create app directory
WORKDIR /app

# Copy Gradle files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code
COPY src src

# Copy crypto directory
COPY crypto /app/crypto
COPY wait-for-it.sh .


# Build the application
RUN ./gradlew build -x test --no-daemon

# Expose the application port
EXPOSE 8080

# Run the application
#ENTRYPOINT ["java", "-jar", "/app/build/libs/client_api.jar"]