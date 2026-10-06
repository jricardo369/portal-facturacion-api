# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine AS runtime
ENV SPRING_PROFILES_ACTIVE=prod
# wget es requerido por el HEALTHCHECK (compose + Dockerfile) y por el loop
# de verificación del workflow deploy.yml. La imagen temurin-alpine no lo trae garantizado.
RUN apk add --no-cache wget
RUN addgroup -S app && adduser -S app -G app
RUN mkdir -p /opt/portal-facturacion-configs && chown app:app /opt/portal-facturacion-configs
WORKDIR /app
COPY --from=build --chown=app:app /app/target/*.jar app.jar
USER app
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
