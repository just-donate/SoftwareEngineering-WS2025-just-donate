# Stage 1: Build the application using sbt-assembly
FROM sbtscala/scala-sbt:eclipse-temurin-21.0.5_11_1.10.7_3.3.4 AS builder
WORKDIR /app

# Copy SBT build config and source
COPY build.sbt ./
COPY project ./project
COPY src ./src

EXPOSE 8080

ENTRYPOINT ["sbt", "run"]