# Stage 1: Build the React Frontend
FROM node:20-alpine AS frontend-builder
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build the Spring Boot Backend (and bundle static React assets)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS backend-builder
WORKDIR /backend
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B
COPY backend/src ./src

# Create resources/static folder and copy frontend built assets
RUN mkdir -p src/main/resources/static
COPY --from=frontend-builder /frontend/dist/ src/main/resources/static/

RUN mvn clean package -DskipTests

# Stage 3: Production JRE runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-builder /backend/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Setup environment variables (defaulting to local SQLite/H2 or container databases)
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
