# syntax=docker/dockerfile:1
# Build context is the REPOSITORY ROOT (the parent POM is needed):
#   docker build -f market-data-service/Dockerfile -t tf-market-data-service .

# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl market-data-service -am clean package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system tradeflow \
    && useradd --system --gid tradeflow --no-create-home tradeflow
WORKDIR /app
COPY --from=build /workspace/market-data-service/target/*.jar app.jar
USER tradeflow
EXPOSE 8082
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -fs http://localhost:8082/actuator/health || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
