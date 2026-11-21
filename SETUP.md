# Setup Guide

## 1. Prerequisites

| Tool | Version | Needed for |
|---|---|---|
| JDK | 17 (Temurin recommended) | building and running services |
| Maven | 3.9+ | building |
| Docker + Docker Compose v2 | recent | local stack, Testcontainers |
| `make` | any | optional shortcuts |
| Kind, kubectl, Helm | optional | local Kubernetes deployment |
| Terraform | 1.6+ (optional) | AWS infrastructure |

Check: `java -version`, `mvn -v`, `docker compose version`.

## 2. Build

```bash
mvn clean verify            # build + tests (Testcontainers tests need Docker)
mvn clean package -DskipTests   # fast build; produces <service>/target/*.jar
mvn clean verify -Pquality  # adds JaCoCo coverage and SpotBugs
```

The parent POM manages every dependency version, so module POMs list artifacts without versions where possible.

## 3. Run everything with Docker Compose

```bash
docker compose up --build -d      # or: make up
docker compose ps                 # wait for all services to be healthy
docker compose logs -f trade-capture-service
docker compose down               # stop (add -v to delete data volumes)
```

Compose starts Postgres 15, Redpanda (Kafka), Redis 7, Prometheus, Grafana and the six services, using health checks and `depends_on: condition: service_healthy`. The first build downloads Maven dependencies and can take several minutes.

| Service | Port |
|---|---|
| trade-capture-service | 8081 |
| market-data-service | 8082 |
| valuation-service | 8083 |
| settlement-service | 8084 |
| reporting-service | 8085 |
| bff-gateway | 8080 |
| postgres | 5432 |
| redpanda Kafka API (host) | 19092 |
| redpanda Schema Registry (host) | 18081 |
| redis | 6379 |
| prometheus | 9090 |
| grafana | 3000 |

Grafana: log in with `admin` / `admin`, then add a Prometheus data source with URL `http://prometheus:9090`. Dashboard JSON exports go in `infra/grafana/dashboards/`.

## 4. Run a single service locally

Start only the infrastructure, then run the service from Maven:

```bash
docker compose up -d postgres redpanda redis
mvn -pl trade-capture-service -am spring-boot:run
```

Defaults in each `application.yml` point at `localhost` (Postgres 5432, Kafka 19092, Redis 6379), so no extra configuration is needed. Replace `trade-capture-service` with any module name.

## 5. Get an ENTSO-E API token

Real day-ahead prices come from the ENTSO-E Transparency Platform (free):

1. Register at https://transparency.entsoe.eu.
2. Email `transparency@entsoe.eu` asking for **RESTful API access** for your account (subject "Restful API access").
3. After approval, open *My Account Settings* and generate a **Web API Security Token**.
4. Provide it to market-data-service as `ENTSOE_API_TOKEN` (planned - the ingestion job is not implemented yet). Never commit it; `.env` is git-ignored.

Portugal bidding zone EIC code: `10YPT-REN------W`.

## 6. Environment variables

| Variable | Used by | Default |
|---|---|---|
| `SPRING_DATASOURCE_URL` | all except bff-gateway | `jdbc:postgresql://localhost:5432/tradeflow?currentSchema=<schema>` |
| `SPRING_DATASOURCE_USERNAME` | same | `tradeflow` |
| `SPRING_DATASOURCE_PASSWORD` | same | `tradeflow` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | trade-capture, market-data, valuation, settlement | `localhost:19092` (`redpanda:9092` in Docker) |
| `SPRING_DATA_REDIS_HOST` / `SPRING_DATA_REDIS_PORT` | market-data, valuation | `localhost` / `6379` |
| `ENTSOE_API_TOKEN` | market-data (planned) | none |
| `NVD_API_KEY` | CI dependency check | none |

Schemas per service: `trades`, `prices`, `valuations`, `settlements` (created by `infra/postgres/init-schemas.sql` on first Postgres start); reporting-service uses `public`.

## 7. Kubernetes (optional)

```bash
kind create cluster --name tradeflow
# build and load images first, e.g.: kind load docker-image tradeflow/trade-capture-service:local --name tradeflow
kubectl apply -k infra/k8s
```

The manifests are placeholders: image names, Postgres/Kafka/Redis endpoints and Secrets still need to be provided. In production, ArgoCD syncs the manifests via the GitOps repository (see `.github/workflows/build-and-deploy.yml`).

## 8. CI/CD

`build-and-deploy.yml` runs build, tests, SonarQube, OWASP Dependency-Check, Trivy, cosign signing, ECR push and a GitOps tag bump. It needs these repository secrets: `AWS_ROLE_ARN`, `ECR_REGISTRY`, `SONAR_TOKEN`, `SONAR_HOST_URL`, `NVD_API_KEY`, `GITOPS_TOKEN`. `nightly-security-scan.yml` runs scheduled dependency and filesystem scans.

## 9. Troubleshooting

| Symptom | Fix |
|---|---|
| `release version 17 not supported` | Maven is using an older JDK; set `JAVA_HOME` to JDK 17 |
| Port already in use | Another process uses 8080-8085, 5432, 6379, 9090 or 3000; stop it or change the host port in `docker-compose.yml` |
| Service exits at startup with a datasource error | Postgres is not up or the schema is missing; run `docker compose up -d postgres`. If Postgres was created before `init-schemas.sql` existed, run `docker compose down -v` |
| Kafka connection errors from the host | Use `localhost:19092` from the host and `redpanda:9092` inside Docker |
| `/actuator/health` returns 401 on reporting-service or bff-gateway | Expected until a `SecurityConfig` permits actuator endpoints; the Docker health check treats 401 as up |
| Testcontainers cannot find Docker | Start Docker Desktop / the daemon; verify with `docker ps` |
| Redpanda fails to start | Needs about 1 GB free memory; close other containers or lower `--memory` in `docker-compose.yml` |
| Docker build behind a proxy or corporate CA | Pass proxy settings as build args and install your CA certificate in an early build layer |
