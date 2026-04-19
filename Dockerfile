# Stage 1: Build stage
FROM maven:3.9.9-eclipse-temurin-25-alpine AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
# We use -DfinalName to ensure the JAR is named exactly what you want
RUN mvn clean package -DskipTests -DfinalName=strava-connect

# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the specific jar name from the build stage
COPY --from=build /app/target/strava-connect.jar strava-connect.jar

# Expose the port (matches your application.yml)
EXPOSE 8080

# Run with --enable-preview as required by your Maven config
ENTRYPOINT ["java", "--enable-preview", "-jar", "strava-connect.jar"]