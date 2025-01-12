# ---------------------------------------------------------------
# Stage 1: Build the application using sbt-assembly
# ---------------------------------------------------------------
FROM sbtscala/scala-sbt:eclipse-temurin-21.0.5_11_1.10.7_3.3.4 AS builder

WORKDIR /app

# Copy SBT build config and sources
COPY build.sbt ./
COPY project ./project
COPY src ./src

# Run sbt-assembly to produce the fat JAR
RUN sbt clean assembly

# ---------------------------------------------------------------
# Stage 2: Run the application
# ---------------------------------------------------------------
FROM eclipse-temurin:11-jre

RUN addgroup -S nonroot \
    && adduser -S nonroot -G nonroot

USER nonroot

WORKDIR /app

# Copy the fat JAR from the builder stage into the runtime image
COPY --from=builder /app/target/scala-3.3.4/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
    