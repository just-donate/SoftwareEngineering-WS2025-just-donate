# Stage 1: Build the application using sbt-assembly
# Build Stage
FROM sbtscala/scala-sbt:eclipse-temurin-21.0.5_11_1.10.7_3.3.4 AS builder
WORKDIR /app

# Copy SBT build config and source
COPY build.sbt ./
COPY project ./project
COPY src ./src

# Build the application
RUN sbt assembly

# Runtime Stage
FROM eclipse-temurin:21.0.5_11-jre
WORKDIR /app

# Copy compiled JAR from the builder stage
COPY --from=builder /app/target/scala-*/*.jar ./app.jar

# Expose the port
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
