# Multi-stage Dockerfile for Gradle
FROM openjdk:17-jdk-slim as builder

# Set working directory
WORKDIR /app

# Install required packages
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Copy Gradle wrapper and build files
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (for layer caching)
RUN ./gradlew --no-daemon dependencies

# Copy source code
COPY src/ src/

# Build the application
RUN ./gradlew --no-daemon clean bootJar

# Runtime stage
FROM openjdk:17-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN useradd -r -s /bin/false empverify

# Set working directory
WORKDIR /app

# Create necessary directories
RUN mkdir -p /app/logs /app/config && \
    chown -R empverify:empverify /app

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/empverify-blockchain-api.jar app.jar

# Change ownership
RUN chown empverify:empverify app.jar

# Switch to non-root user
USER empverify

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/employment-records/health || exit 1

# Run the application
CMD ["java", "-jar", "app.jar"]