# Board Backend API

REST API para um quadro Kanban pessoal (estilo Trello), com suporte completo a CRUD de Boards, Colunas e Cards.

## Stack

- **Java 21** + **Spring Boot 3.3**
- **Spring Data JPA** + **H2** (in-memory)
- **Spring Validation** (Bean Validation 3)
- **Maven 3**
- Testes: **JUnit 5** + **Mockito** + **TestRestTemplate**

## Pré-requisitos

- Java 21+
- Maven 3.8+

## Executar

```bash
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

Console do banco H2 disponível em `http://localhost:8080/h2-console`
(JDBC URL: `jdbc:h2:mem:boarddb`, usuário: `sa`, senha vazia).

## Testes

```bash
./mvnw test
```

## Endpoints

### Boards

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/boards` | Lista todos os boards |
| `POST` | `/api/boards` | Cria um board |
| `PATCH` | `/api/boards/{id}` | Renomeia um board |
| `DELETE` | `/api/boards/{id}` | Remove um board (cascade) |

### Colunas

| Método | Rota | Descrição |
|--------|------|-----------|
| `GET` | `/api/boards/{boardId}/columns` | Lista colunas do board (ordenadas por posição) |
| `POST` | `/api/boards/{boardId}/columns` | Cria uma coluna |
| `PATCH` | `/api/columns/{id}` | Atualiza nome ou posição |
| `DELETE` | `/api/columns/{id}` | Remove coluna (cascade cards) |

### Cards

| Método | Rota | Descrição |
|--------|------|-----------|
| `POST` | `/api/columns/{columnId}/cards` | Cria um card |
| `PATCH` | `/api/cards/{id}` | Atualiza título, descrição, label, posição, coluna ou status |
| `DELETE` | `/api/cards/{id}` | Remove um card |

## Modelo de dados

```
Board
 └── BoardColumn (posição auto-calculada: MAX + 1)
      └── Card (posição auto-calculada: MAX + 1)
```

- Deleção em cascata: remover Board remove colunas e cards; remover coluna remove cards.
- Posicionamento controlado pelo frontend — o backend persiste os valores recebidos sem reordenar.

## Estrutura do projeto

```
src/
├── main/java/com/example/board/
│   ├── config/          # CORS, GlobalExceptionHandler
│   ├── controller/      # BoardController, ColumnController, CardController
│   ├── service/         # regras de negócio
│   ├── repository/      # Spring Data JPA
│   ├── model/           # entidades JPA
│   └── dto/             # request/response DTOs
└── test/
    ├── controller/      # testes de integração (SpringBootTest + TestRestTemplate)
    └── service/         # testes unitários (Mockito)
```

## CORS

Por padrão, aceita requisições de `http://localhost:4200` (frontend Angular).

## Observações

- Banco em memória: os dados são perdidos ao reiniciar.
- Troca para PostgreSQL: ajuste as propriedades em `src/main/resources/application.yml` sem alterar código.
- Projeto single-user MVP — sem autenticação.
