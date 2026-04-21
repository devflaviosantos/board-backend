# Modelo de Configuração: Deploy Contínuo do Backend no Railway

**Branch**: `002-backend-deploy-railway` | **Data**: 2026-04-21

Esta funcionalidade é de infraestrutura/DevOps — os "artefatos" são arquivos
de configuração, não entidades de banco de dados. Este documento descreve a
estrutura e os campos de cada artefato a ser criado ou modificado.

---

## Artefato 1: Dockerfile

**Localização**: `./Dockerfile` (raiz do repositório)

```
Stage: build
  Base image : maven:3.9-eclipse-temurin-21
  Workdir    : /build
  Ação       : Copia pom.xml e src/, roda `mvn package -DskipTests`
  Saída      : target/*.jar

Stage: runtime
  Base image : eclipse-temurin:21-jre-alpine
  Workdir    : /app
  Copia      : JAR do stage build para /app/app.jar
  EXPOSE     : 8080
  ENTRYPOINT : ["java", "-jar", "app.jar"]
```

**Campos relevantes**:
| Campo | Valor | Justificativa |
|-------|-------|---------------|
| Base build | `maven:3.9-eclipse-temurin-21` | JDK 21 + Maven 3.9, oficial Temurin |
| Base runtime | `eclipse-temurin:21-jre-alpine` | JRE mínimo (~180 MB) sem Maven |
| EXPOSE | `8080` | Porta padrão Spring Boot (Railway usa PORT env var) |
| ENTRYPOINT | `java -jar app.jar` | Execução direta; Railway injeta PORT e DB vars |

---

## Artefato 2: railway.json

**Localização**: `./railway.json` (raiz do repositório)

```json
{
  "$schema": "https://railway.app/railway.schema.json",
  "build": {
    "dockerfilePath": "Dockerfile"
  },
  "deploy": {
    "healthcheckPath": "/actuator/health",
    "healthcheckTimeout": 30,
    "restartPolicyType": "ON_FAILURE",
    "restartPolicyMaxRetries": 3
  }
}
```

**Campos**:
| Campo | Valor | Justificativa |
|-------|-------|---------------|
| `dockerfilePath` | `"Dockerfile"` | Aponta para o Dockerfile na raiz |
| `healthcheckPath` | `"/actuator/health"` | Endpoint Spring Actuator para liveness |
| `healthcheckTimeout` | `30` | Segundos de espera antes de considerar falha |
| `restartPolicyType` | `"ON_FAILURE"` | Reinicia somente em caso de crash |

---

## Artefato 3: pom.xml — dependências a adicionar

**Localização**: `./pom.xml`

**Dependências novas** (inserir dentro de `<dependencies>`):

```xml
<!-- Healthcheck para Railway -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- Driver PostgreSQL para produção -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

**Campos**:
| Dependência | Escopo | Propósito |
|-------------|--------|-----------|
| `spring-boot-starter-actuator` | `compile` (padrão) | Expõe `/actuator/health` |
| `postgresql` | `runtime` | Driver JDBC ativado somente em runtime |

---

## Artefato 4: application.yml — perfil dev explícito

**Localização**: `src/main/resources/application.yml`

Adicionar `spring.config.activate.on-profile: dev` e ajustar `server.port`
para usar variável de ambiente como fallback. O restante permanece igual.

```yaml
spring:
  config:
    activate:
      on-profile: "!prod"        # ativa quando o perfil prod NÃO está ativo
  datasource:
    url: jdbc:h2:mem:boarddb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    open-in-view: false

server:
  port: ${PORT:8080}
```

---

## Artefato 5: application-prod.yml — perfil de produção

**Localização**: `src/main/resources/application-prod.yml`

```yaml
spring:
  datasource:
    url: "jdbc:${DATABASE_URL}"
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    open-in-view: false
    database-platform: org.hibernate.dialect.PostgreSQLDialect
  h2:
    console:
      enabled: false

server:
  port: ${PORT:8080}

logging:
  level:
    root: INFO
    com.example.board: INFO
```

**Campos**:
| Campo | Valor | Justificativa |
|-------|-------|---------------|
| `datasource.url` | `jdbc:${DATABASE_URL}` | Railway injeta `DATABASE_URL` no formato `postgresql://...`; prefixar com `jdbc:` cria URL válida para o driver JDBC |
| `ddl-auto` | `update` | Preserva dados entre deploys; cria/altera tabelas sem dropar |
| `h2.console.enabled` | `false` | Console H2 desativado em produção por segurança |
| `show-sql` | `false` | Logs limpos em produção |
| `server.port` | `${PORT:8080}` | Railway injeta `PORT`; fallback 8080 para testes locais |

---

## Artefato 6: deploy-backend.yml — workflow GitHub Actions

**Localização**: `.github/workflows/deploy-backend.yml`

**Estrutura dos jobs**:

```
Trigger
  on.push
    branches: [main]
    paths: [src/**, pom.xml, Dockerfile, railway.json,
            .github/workflows/deploy-backend.yml]

Job: test
  runs-on: ubuntu-latest
  steps:
    1. actions/checkout@v4
    2. actions/setup-java@v4 (java-version: 21, distribution: temurin)
    3. actions/cache@v4 (path: ~/.m2, key: maven-${{ hashFiles('**/pom.xml') }})
    4. mvn test

Job: build-and-push
  needs: [test]
  runs-on: ubuntu-latest
  steps:
    1. actions/checkout@v4
    2. docker/login-action@v3 (registry: ghcr.io, token: GHCR_TOKEN)
    3. docker/build-push-action@v5
       context: .
       file: ./Dockerfile
       push: true
       tags: ghcr.io/${{ github.repository_owner }}/${{ github.event.repository.name }}-backend:latest

Job: deploy
  needs: [build-and-push]
  runs-on: ubuntu-latest
  steps:
    1. railway-app/railway-action@v1
       env:
         RAILWAY_TOKEN: ${{ secrets.RAILWAY_TOKEN }}
         RAILWAY_SERVICE: backend
```

**Variáveis e segredos**:
| Nome | Onde configurar | Tipo | Descrição |
|------|----------------|------|-----------|
| `RAILWAY_TOKEN` | GitHub Secrets | Secret | Token de deploy do Railway |
| `GHCR_TOKEN` | GitHub Secrets | Secret | PAT com escopo `write:packages` |
| `DATABASE_URL` | Railway dashboard | Env var | Gerado pelo plugin PostgreSQL |
| `SPRING_PROFILES_ACTIVE` | Railway dashboard | Env var | Valor: `prod` |
