# =============================================================================
# Build multi-stage: garante uma imagem final enxuta (apenas JRE + o jar),
# sem carregar o Maven e as dependências de build na imagem de produção —
# fundamental para escalabilidade e portabilidade (deploy rápido, imagens
# menores para orquestradores como Kubernetes/Docker Swarm).
# =============================================================================

# ---- Etapa 1: build -----------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copia primeiro o pom.xml para aproveitar o cache de camadas do Docker: as
# dependências só são baixadas novamente se o pom.xml mudar.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# ---- Etapa 2: runtime -----------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Usuário não-root por segurança.
RUN addgroup -S biblioteca && adduser -S biblioteca -G biblioteca
USER biblioteca

COPY --from=build /build/target/biblioteca-api.jar app.jar

ENV TZ=UTC \
    JAVA_OPTS=""

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
