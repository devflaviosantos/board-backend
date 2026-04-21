/speckit.specify

Quero construir o BACKEND de um sistema de board estilo Trello para uso pessoal.
Linguagem: Java 21. Framework: Spring Boot 3.3.

--- FUNCIONALIDADES ---

Boards:
- Criar, renomear e excluir boards
- Listar todos os boards

Colunas:
- Cada board tem N colunas (ex: "A fazer", "Em andamento", "Concluído")
- Criar, renomear, reordenar e excluir colunas dentro de um board

Cartões (cards):
- Criar cartão com título, descrição opcional, label/cor e posição
- Mover cartão entre colunas (atualizar columnId + position via PATCH)
- Reordenar cartões dentro da mesma coluna
- Excluir cartão

--- STACK ---

- Java 21
- Spring Boot 3.3
- Spring Data JPA
- Banco H2 in-memory (MVP — fácil trocar por PostgreSQL depois)
- Spring Validation para @NotBlank nos títulos
- CORS liberado para http://localhost:4200

--- ENTIDADES E CAMPOS ---

Board:
  id (Long, auto)
  name (String, not blank)
  createdAt (LocalDateTime)

Column:
  id (Long, auto)
  boardId (Long, FK → Board)
  name (String, not blank)
  position (Integer — para ordenação)

Card:
  id (Long, auto)
  columnId (Long, FK → Column)
  title (String, not blank)
  description (String, nullable)
  label (String, nullable — ex: "bug", "feature", "urgente")
  position (Integer — para ordenação dentro da coluna)
  completed (boolean, default false)
  createdAt (LocalDateTime)

--- ENDPOINTS REST ---

# Boards
GET    /api/boards                       → lista todos os boards
POST   /api/boards                       → cria board (body: { name })
PATCH  /api/boards/{id}                  → renomeia board (body: { name })
DELETE /api/boards/{id}                  → remove board (cascata: remove colunas e cards)

# Colunas
GET    /api/boards/{boardId}/columns     → lista colunas do board ordenadas por position, cada uma com seus cards
POST   /api/boards/{boardId}/columns     → cria coluna (body: { name })
PATCH  /api/columns/{id}                 → renomeia ou atualiza position (body: { name?, position? })
DELETE /api/columns/{id}                 → remove coluna (cascata: remove cards)

# Cards
POST   /api/columns/{columnId}/cards     → cria card (body: { title, description?, label? })
PATCH  /api/cards/{id}                   → atualiza title, description, label, position ou columnId
DELETE /api/cards/{id}                   → remove card

--- REGRAS DE NEGÓCIO ---

- Ao criar coluna, position = max(position das colunas do board) + 1
- Ao criar card, position = max(position dos cards da coluna) + 1
- Ao mover/reordenar, o frontend envia o novo position e/ou columnId; o backend apenas persiste
- Cascata: deletar Board remove Columns e Cards; deletar Column remove Cards
- Título vazio retorna 400 Bad Request com mensagem descritiva

--- ESTRUTURA DE PACOTES ESPERADA ---

src/main/java/com/exemplo/board/
├── controller/
│   ├── BoardController.java
│   ├── ColumnController.java
│   └── CardController.java
├── service/
│   ├── BoardService.java
│   ├── ColumnService.java
│   └── CardService.java
├── repository/
│   ├── BoardRepository.java
│   ├── ColumnRepository.java
│   └── CardRepository.java
├── model/
│   ├── Board.java
│   ├── Column.java   (cuidado: "Column" é palavra reservada SQL — usar @Table(name="columns") ou renomear para BoardColumn)
│   └── Card.java
├── dto/
│   ├── BoardDto.java
│   ├── ColumnDto.java
│   └── CardDto.java
└── BoardApplication.java

src/main/resources/
└── application.yml

--- CRITÉRIOS DE ACEITE ---

- [ ] GET /api/boards retorna lista de boards em JSON
- [ ] POST /api/boards cria board e retorna 201 com objeto
- [ ] GET /api/boards/{id}/columns retorna colunas com cards aninhados, ordenados por position
- [ ] POST /api/boards/{id}/columns cria coluna e retorna 201
- [ ] POST /api/columns/{id}/cards cria card e retorna 201
- [ ] PATCH /api/cards/{id} aceita columnId e position (para drag-and-drop do frontend)
- [ ] DELETE em cascade funciona corretamente
- [ ] Título vazio retorna 400 com mensagem de erro
- [ ] H2 console disponível em /h2-console para debug

--- FORA DO ESCOPO v1 ---
- Autenticação / login
- Múltiplos usuários
- Upload de arquivos
- Notificações
- Banco externo (usar H2 por ora)


--- PRÓXIMOS PASSOS APÓS O SPECIFY ---

/speckit.plan   Java 21, Spring Boot 3.3, JPA, H2, Validation, CORS para :4200
/speckit.tasks
/speckit.implement
