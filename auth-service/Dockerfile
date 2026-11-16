FROM gradle:8.10-jdk21 AS build
WORKDIR /workspace
COPY settings.gradle ./
COPY gradle ./gradle
COPY auth-service ./auth-service
RUN gradle :auth-service:build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /workspace/auth-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
