# Stage 1: Build the frontend
FROM node:18-alpine AS frontend-builder

# Set working directory for frontend
WORKDIR /app/frontend

# Copy frontend dependency files
COPY frontend/package.json frontend/package-lock.json ./

# Install frontend dependencies
RUN npm ci

# Copy the rest of the frontend source code
COPY frontend/ ./

# Build the frontend
RUN npm run build

# Stage 2: Build the backend
FROM gradle:7.6-jdk17 AS backend-builder

# Set working directory for backend
WORKDIR /home/gradle/project

# Copy backend build files
COPY backend/build.gradle backend/settings.gradle ./
COPY backend/gradle gradle

# Download backend dependencies
RUN gradle build -x test --no-daemon

# Copy backend source code
COPY backend/src backend/src

# Copy the built frontend files into the backend's static resources
COPY --from=frontend-builder /app/frontend/dist /home/gradle/project/src/main/resources/static

# Build the backend JAR
RUN gradle build --no-daemon

# Stage 3: Create the final image
FROM openjdk:21-jdk

# Set working directory
WORKDIR /app

# Copy the built JAR from the backend builder stage
COPY --from=backend-builder /home/gradle/project/build/libs/*.jar app.jar

# Expose the application's port (adjust if different)
EXPOSE 8080

# Define the entry point to run the JAR
# ENTRYPOINT ["java", "-jar", "app.jar"]

