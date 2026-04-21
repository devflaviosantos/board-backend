# Modelo de Dados: Backend do Quadro Kanban Pessoal

**Branch**: `001-board-backend-api` | **Data**: 2026-04-20

## Entidades

### Board (Quadro)

Espaço de trabalho de nível superior que agrupa colunas.

| Campo       | Tipo            | Restrições                        |
|-------------|-----------------|-----------------------------------|
| id          | Long            | PK, auto-gerado                   |
| name        | String          | NOT NULL, NOT BLANK               |
| createdAt   | LocalDateTime   | NOT NULL, atribuído automaticamente na criação |

**Relacionamentos**:
- `1:N` com `BoardColumn` — cascade ALL, orphanRemoval = true

---

### BoardColumn (Coluna)

Estágio de fluxo de trabalho dentro de um quadro (ex: "A Fazer", "Em Andamento").

Nome da classe Java: `BoardColumn`
Nome da tabela SQL: `columns`

| Campo     | Tipo      | Restrições                                    |
|-----------|-----------|-----------------------------------------------|
| id        | Long      | PK, auto-gerado                               |
| board     | Board     | FK → boards.id, NOT NULL, eager load desaconselhado |
| name      | String    | NOT NULL, NOT BLANK                           |
| position  | Integer   | NOT NULL, auto-atribuído na criação (MAX + 1 dentro do board) |

**Relacionamentos**:
- `N:1` com `Board`
- `1:N` com `Card` — cascade ALL, orphanRemoval = true

---

### Card (Cartão)

Item de tarefa individual dentro de uma coluna.

| Campo       | Tipo          | Restrições                                          |
|-------------|---------------|-----------------------------------------------------|
| id          | Long          | PK, auto-gerado                                     |
| column      | BoardColumn   | FK → columns.id, NOT NULL                          |
| title       | String        | NOT NULL, NOT BLANK                                 |
| description | String        | Nullable                                            |
| label       | String        | Nullable, texto livre (ex: "bug", "feature", "urgente") |
| position    | Integer       | NOT NULL, auto-atribuído na criação (MAX + 1 dentro da coluna) |
| completed   | boolean       | NOT NULL, default = false                           |
| createdAt   | LocalDateTime | NOT NULL, atribuído automaticamente na criação      |

**Relacionamentos**:
- `N:1` com `BoardColumn`

---

## Diagrama de Relacionamentos

```
Board (1) ──────────< BoardColumn (N)
  id                       id
  name                     board_id (FK)
  createdAt                name
                           position
                           │
                           │
                    BoardColumn (1) ──────────< Card (N)
                                                  id
                                                  column_id (FK)
                                                  title
                                                  description
                                                  label
                                                  position
                                                  completed
                                                  createdAt
```

---

## Regras de Negócio de Dados

1. **Auto-posição na criação de Column**: `position = SELECT COALESCE(MAX(position), 0) + 1 FROM columns WHERE board_id = :boardId`
2. **Auto-posição na criação de Card**: `position = SELECT COALESCE(MAX(position), 0) + 1 FROM cards WHERE column_id = :columnId`
3. **Cascade delete Board**: Remove todas as BoardColumns e todos os Cards associados
4. **Cascade delete Column**: Remove todos os Cards da coluna
5. **Movimentação de Card**: Atualizar `column_id` e/ou `position` conforme enviado pelo frontend; sem reordenação automática de outros cartões
6. **Validação de campos obrigatórios**: `name` (Board, BoardColumn) e `title` (Card) rejeitam strings em branco com 400 Bad Request

---

## DTOs (Contratos de Transferência)

### BoardDto (Response)
```
id: Long
name: String
createdAt: LocalDateTime
```

### BoardRequest (Request — criar/renomear)
```
name: String  [obrigatório, @NotBlank]
```

### ColumnDto (Response)
```
id: Long
boardId: Long
name: String
position: Integer
cards: List<CardDto>
```

### ColumnRequest (Request — criar/atualizar)
```
name: String?     [obrigatório para criação, @NotBlank se presente]
position: Integer?
```

### CardDto (Response)
```
id: Long
columnId: Long
title: String
description: String?
label: String?
position: Integer
completed: boolean
createdAt: LocalDateTime
```

### CardRequest (Request — criar)
```
title: String        [obrigatório, @NotBlank]
description: String? [opcional]
label: String?       [opcional]
```

### CardUpdateRequest (Request — atualizar via PATCH)
```
title: String?       [opcional, @NotBlank se presente]
description: String? [opcional]
label: String?       [opcional]
position: Integer?   [opcional]
columnId: Long?      [opcional — para mover entre colunas]
completed: Boolean?  [opcional]
```

---

## Schema SQL (gerado automaticamente pelo Hibernate)

```sql
CREATE TABLE boards (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL
);

CREATE TABLE columns (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    board_id BIGINT       NOT NULL REFERENCES boards(id),
    name     VARCHAR(255) NOT NULL,
    position INTEGER      NOT NULL
);

CREATE TABLE cards (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    column_id   BIGINT       NOT NULL REFERENCES columns(id),
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    label       VARCHAR(100),
    position    INTEGER      NOT NULL,
    completed   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL
);
```
