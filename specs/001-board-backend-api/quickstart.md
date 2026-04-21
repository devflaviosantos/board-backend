# Quickstart: Backend do Quadro Kanban Pessoal

**Branch**: `001-board-backend-api` | **Data**: 2026-04-20

## Pré-requisitos

- Java 21+
- Maven 3.9+ (ou use o Maven Wrapper `./mvnw`)
- (Opcional) IDE com suporte a Spring Boot — IntelliJ IDEA ou VS Code com extensão Java

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/example/board/
│   │   ├── BoardApplication.java
│   │   ├── config/
│   │   │   ├── CorsConfig.java
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── controller/
│   │   │   ├── BoardController.java
│   │   │   ├── ColumnController.java
│   │   │   └── CardController.java
│   │   ├── dto/
│   │   │   ├── BoardDto.java
│   │   │   ├── BoardRequest.java
│   │   │   ├── CardDto.java
│   │   │   ├── CardRequest.java
│   │   │   ├── CardUpdateRequest.java
│   │   │   ├── ColumnDto.java
│   │   │   └── ColumnRequest.java
│   │   ├── model/
│   │   │   ├── Board.java
│   │   │   ├── BoardColumn.java
│   │   │   └── Card.java
│   │   ├── repository/
│   │   │   ├── BoardRepository.java
│   │   │   ├── ColumnRepository.java
│   │   │   └── CardRepository.java
│   │   └── service/
│   │       ├── BoardService.java
│   │       ├── ColumnService.java
│   │       └── CardService.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/com/example/board/
        ├── controller/
        │   ├── BoardControllerTest.java
        │   ├── ColumnControllerTest.java
        │   └── CardControllerTest.java
        └── service/
            ├── BoardServiceTest.java
            ├── ColumnServiceTest.java
            └── CardServiceTest.java
```

## Dependências (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Configuração (application.yml)

```yaml
spring:
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
  port: 8080
```

## Como Executar

```bash
# Compilar e executar
./mvnw spring-boot:run

# Ou com Maven instalado globalmente
mvn spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Como Testar

```bash
# Executar todos os testes
./mvnw test

# Executar testes de uma classe específica
./mvnw test -Dtest=BoardControllerTest
```

## Verificação Rápida (curl)

```bash
# Criar um quadro
curl -s -X POST http://localhost:8080/api/boards \
  -H "Content-Type: application/json" \
  -d '{"name":"Meu Projeto"}' | jq

# Listar quadros
curl -s http://localhost:8080/api/boards | jq

# Criar uma coluna
curl -s -X POST http://localhost:8080/api/boards/1/columns \
  -H "Content-Type: application/json" \
  -d '{"name":"A Fazer"}' | jq

# Criar um cartão
curl -s -X POST http://localhost:8080/api/columns/1/cards \
  -H "Content-Type: application/json" \
  -d '{"title":"Primeira tarefa","label":"feature"}' | jq

# Mover cartão para outra coluna (drag-and-drop)
curl -s -X PATCH http://localhost:8080/api/cards/1 \
  -H "Content-Type: application/json" \
  -d '{"columnId":2,"position":1}' | jq

# Verificar console H2
open http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:boarddb
```

## Trocar para PostgreSQL (pós-MVP)

Substituir no `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/boarddb
    driver-class-name: org.postgresql.Driver
    username: postgres
    password: sua_senha
  jpa:
    hibernate:
      ddl-auto: validate  # ou update
    database-platform: org.hibernate.dialect.PostgreSQLDialect
```

Adicionar dependência no `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```
