# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app/Resume-Sensei
COPY Resume-Sensei/pom.xml .
RUN mvn dependency:go-offline
# Copy source code
COPY Resume-Sensei/src ./src
# Build the application
RUN mvn clean package -DskipTests

# ---- Run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/Resume-Sensei/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]