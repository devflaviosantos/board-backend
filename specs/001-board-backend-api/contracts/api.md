# Contrato da API REST: Backend do Quadro Kanban Pessoal

**Branch**: `001-board-backend-api` | **Data**: 2026-04-20  
**Base URL**: `http://localhost:8080`  
**Content-Type**: `application/json`  
**CORS**: `http://localhost:4200` (origem permitida)

---

## Boards (Quadros)

### GET /api/boards
Lista todos os quadros.

**Response 200**:
```json
[
  {
    "id": 1,
    "name": "Meu Projeto",
    "createdAt": "2026-04-20T10:00:00"
  }
]
```

---

### POST /api/boards
Cria um novo quadro.

**Request**:
```json
{ "name": "Meu Projeto" }
```

**Response 201**:
```json
{
  "id": 1,
  "name": "Meu Projeto",
  "createdAt": "2026-04-20T10:00:00"
}
```

**Response 400** (name em branco):
```json
{
  "errors": [
    { "field": "name", "message": "não pode ser em branco" }
  ]
}
```

---

### PATCH /api/boards/{id}
Renomeia um quadro existente.

**Path param**: `id` — identificador do quadro

**Request**:
```json
{ "name": "Novo Nome" }
```

**Response 200**: BoardDto atualizado  
**Response 400**: name em branco  
**Response 404**: quadro não encontrado

---

### DELETE /api/boards/{id}
Remove um quadro e todos os seus dados (colunas e cartões em cascata).

**Path param**: `id` — identificador do quadro

**Response 204**: sem corpo  
**Response 404**: quadro não encontrado

---

## Columns (Colunas)

### GET /api/boards/{boardId}/columns
Lista todas as colunas de um quadro, ordenadas por `position` crescente. Cada coluna inclui seus cartões também ordenados por `position`.

**Path param**: `boardId`

**Response 200**:
```json
[
  {
    "id": 1,
    "boardId": 1,
    "name": "A Fazer",
    "position": 1,
    "cards": [
      {
        "id": 1,
        "columnId": 1,
        "title": "Tarefa 1",
        "description": "Detalhe da tarefa",
        "label": "feature",
        "position": 1,
        "completed": false,
        "createdAt": "2026-04-20T10:05:00"
      }
    ]
  },
  {
    "id": 2,
    "boardId": 1,
    "name": "Em Andamento",
    "position": 2,
    "cards": []
  }
]
```

**Response 404**: quadro não encontrado

---

### POST /api/boards/{boardId}/columns
Cria uma nova coluna no quadro. A posição é atribuída automaticamente (MAX + 1).

**Path param**: `boardId`

**Request**:
```json
{ "name": "A Fazer" }
```

**Response 201**:
```json
{
  "id": 1,
  "boardId": 1,
  "name": "A Fazer",
  "position": 1,
  "cards": []
}
```

**Response 400**: name em branco  
**Response 404**: quadro não encontrado

---

### PATCH /api/columns/{id}
Renomeia uma coluna e/ou atualiza sua posição.

**Path param**: `id`

**Request** (todos os campos opcionais, ao menos um obrigatório):
```json
{ "name": "Novo Nome", "position": 3 }
```

**Response 200**: ColumnDto atualizado  
**Response 400**: name em branco (se fornecido)  
**Response 404**: coluna não encontrada

---

### DELETE /api/columns/{id}
Remove uma coluna e todos os seus cartões (cascata).

**Path param**: `id`

**Response 204**: sem corpo  
**Response 404**: coluna não encontrada

---

## Cards (Cartões)

### POST /api/columns/{columnId}/cards
Cria um novo cartão na coluna. A posição é atribuída automaticamente (MAX + 1).

**Path param**: `columnId`

**Request**:
```json
{
  "title": "Implementar login",
  "description": "Usar JWT com refresh token",
  "label": "feature"
}
```

**Response 201**:
```json
{
  "id": 1,
  "columnId": 1,
  "title": "Implementar login",
  "description": "Usar JWT com refresh token",
  "label": "feature",
  "position": 1,
  "completed": false,
  "createdAt": "2026-04-20T10:10:00"
}
```

**Response 400**: title em branco  
**Response 404**: coluna não encontrada

---

### PATCH /api/cards/{id}
Atualiza um cartão. Suporta atualização parcial (todos os campos são opcionais). Usado para renomear, mover entre colunas, reordenar e marcar como concluído.

**Path param**: `id`

**Request** (todos os campos opcionais):
```json
{
  "title": "Novo título",
  "description": "Nova descrição",
  "label": "bug",
  "position": 2,
  "columnId": 3,
  "completed": true
}
```

**Caso de uso — drag-and-drop (mover para outra coluna)**:
```json
{ "columnId": 3, "position": 1 }
```

**Caso de uso — reordenar na mesma coluna**:
```json
{ "position": 4 }
```

**Caso de uso — marcar como concluído**:
```json
{ "completed": true }
```

**Response 200**: CardDto atualizado  
**Response 400**: title em branco (se fornecido)  
**Response 404**: cartão não encontrado

---

### DELETE /api/cards/{id}
Remove um cartão permanentemente.

**Path param**: `id`

**Response 204**: sem corpo  
**Response 404**: cartão não encontrado

---

## Tratamento de Erros

### 400 Bad Request — Validação
```json
{
  "errors": [
    { "field": "title", "message": "não pode ser em branco" }
  ]
}
```

### 404 Not Found
```json
{
  "error": "Board não encontrado com id: 99"
}
```

---

## Outros Endpoints

### GET /h2-console
Console web do banco H2 para inspeção em desenvolvimento.  
**JDBC URL**: `jdbc:h2:mem:boarddb`  
**User**: `sa` | **Password**: (em branco)
