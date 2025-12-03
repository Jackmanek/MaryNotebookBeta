# Etapa 1: Build con Maven + JDK 22
FROM maven:3.9.6-eclipse-temurin-22 AS builder

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

# Etapa 2: Runtime con JRE ligero
FROM amazoncorretto:22-alpine
WORKDIR /app

# crear usuario
RUN addgroup appgroup && adduser -S appuser -G appgroup && \
    mkdir -p /app/uploads && \
    chown appuser:appgroup /app/uploads
USER appuser

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
