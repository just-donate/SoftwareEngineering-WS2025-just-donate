FROM sbt:1.9.3-jdk17 AS builder
WORKDIR /app

# Copy SBT build config and source
COPY build.sbt ./
COPY project ./project
COPY src ./src

# Build your fat JAR (requires sbt-assembly plugin)
RUN sbt clean assembly

# ---
FROM openjdk:17-jdk-slim
WORKDIR /app

# Copy the assembled JAR from the builder stage
COPY --from=builder /app/target/scala-3.*/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]