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
RUN addgroup -g 1000 appgroup && \
    adduser -D -u 1000 -G appgroup appuser && \
    mkdir -p /uploads /app/wallet && \
    chown -R appuser:appgroup /uploads /app

COPY --chown=appuser:appgroup wallet/ /app/wallet/

USER appuser

COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV FILE_UPLOAD_DIR=/uploads

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
