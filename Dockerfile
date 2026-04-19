# Stage 1: Build stage
# We use a standard Maven image to "borrow" the Maven binaries,
# but run the build on a JDK 25 Alpine base.
FROM maven:3.9.9-eclipse-temurin-21-alpine AS maven_binaries
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Copy Maven from the official image to our JDK 25 environment
COPY --from=maven_binaries /usr/share/maven /usr/share/maven
COPY --from=maven_binaries /usr/local/bin/mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# Copy your project files
COPY pom.xml .
COPY src ./src

# Build the application with Java 25 preview features
RUN mvn clean package -DskipTests -DfinalName=strava-connect

# Stage 2: Runtime stage
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Copy the generated jar
COPY --from=build /app/target/strava-connect.jar strava-connect.jar

EXPOSE 8080

# Essential: Keep --enable-preview for Java 25/Spring AI 2.0.0
ENTRYPOINT ["java", "--enable-preview", "-jar", "strava-connect.jar"]