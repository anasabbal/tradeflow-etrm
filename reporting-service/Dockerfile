# syntax=docker/dockerfile:1
# Build context is the REPOSITORY ROOT (the parent POM is needed):
#   docker build -f reporting-service/Dockerfile -t tf-reporting-service .

# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY . .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl reporting-service -am clean package -DskipTests

# ---------- runtime stage ----------
FROM eclipse-temurin:17-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system tradeflow \
    && useradd --system --gid tradeflow --no-create-home tradeflow
WORKDIR /app
COPY --from=build /workspace/reporting-service/target/*.jar app.jar
USER tradeflow
EXPOSE 8085
# NOTE: Spring Security protects /actuator/** until a SecurityConfig is added, so the
# probe treats 401 as "server is up". Tighten to 200 once actuator is permitted.
HEALTHCHECK --interval=15s --timeout=5s --start-period=40s --retries=5 \
  CMD curl -s -o /dev/null -w "%{http_code}" http://localhost:8085/actuator/health | grep -Eq "^(200|401)$" || exit 1
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/app.jar"]
