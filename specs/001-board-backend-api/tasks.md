# Tarefas: Backend do Quadro Kanban Pessoal

**Input**: Documentos de design em `specs/001-board-backend-api/`
**Pré-requisitos**: plan.md, spec.md, data-model.md, contracts/api.md, research.md

**Organização**: Tarefas agrupadas por história de usuário para permitir implementação e teste independentes de cada história.

## Formato: `[ID] [P?] [Story?] Descrição com caminho do arquivo`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências em tarefas incompletas)
- **[Story]**: À qual história de usuário esta tarefa pertence (US1, US2, US3, US4)
- Caminhos base: `src/main/java/com/example/board/` e `src/main/resources/`

---

## Fase 1: Setup (Infraestrutura do Projeto)

**Objetivo**: Inicializar o projeto Maven com todas as dependências e estrutura de pacotes

- [x] T001 Criar `pom.xml` com Java 21, Spring Boot 3.3, spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-validation, h2 (runtime), spring-boot-starter-test na raiz do repositório
- [x] T002 Criar estrutura de pacotes vazios: `src/main/java/com/example/board/{controller,service,repository,model,dto,config}/`
- [x] T003 [P] Criar `src/main/resources/application.yml` com H2 in-memory (`jdbc:h2:mem:boarddb;DB_CLOSE_DELAY=-1`), console em `/h2-console`, `ddl-auto: create-drop`, `show-sql: true`, `open-in-view: false`
- [x] T004 [P] Criar `src/main/java/com/example/board/BoardApplication.java` com `@SpringBootApplication` e método `main`

---

## Fase 2: Fundação (Pré-requisitos Bloqueantes)

**Objetivo**: Infraestrutura transversal necessária antes de qualquer história de usuário

**⚠️ CRÍTICO**: Nenhuma história de usuário pode começar antes desta fase estar completa

- [x] T005 Criar `src/main/java/com/example/board/config/ResourceNotFoundException.java` como `@ResponseStatus(HttpStatus.NOT_FOUND)` extends `RuntimeException` com mensagem formatada
- [x] T006 [P] Criar `src/main/java/com/example/board/config/GlobalExceptionHandler.java` com `@ControllerAdvice` que trata `MethodArgumentNotValidException` (400 com lista de erros de campo) e `ResourceNotFoundException` (404 com mensagem)
- [x] T007 [P] Criar `src/main/java/com/example/board/config/CorsConfig.java` implementando `WebMvcConfigurer`, permitindo origem `http://localhost:4200`, todos os métodos HTTP e todos os headers para `/**`

**Checkpoint**: Fundação pronta — implementação das histórias de usuário pode começar

---

## Fase 3: História de Usuário 1 — Gerenciar Quadros (P1) 🎯 MVP

**Objetivo**: CRUD completo de quadros — criar, listar, renomear e excluir

**Teste Independente**: Com apenas este código, `GET /api/boards`, `POST /api/boards`, `PATCH /api/boards/{id}` e `DELETE /api/boards/{id}` devem funcionar corretamente, incluindo rejeição de nome em branco com 400

### Implementação

- [x] T008 [P] [US1] Criar `src/main/java/com/example/board/model/Board.java` com `@Entity`, `@Table(name="boards")`, campos: `id` (Long, `@Id @GeneratedValue`), `name` (String, `@NotBlank`), `createdAt` (LocalDateTime, `@CreationTimestamp`)
- [x] T009 [P] [US1] Criar `src/main/java/com/example/board/dto/BoardDto.java` (id, name, createdAt) e `src/main/java/com/example/board/dto/BoardRequest.java` (name com `@NotBlank`)
- [x] T010 [US1] Criar `src/main/java/com/example/board/repository/BoardRepository.java` extendendo `JpaRepository<Board, Long>`
- [x] T011 [US1] Criar `src/main/java/com/example/board/service/BoardService.java` com métodos: `findAll()` → `List<BoardDto>`, `findById(Long)` → `BoardDto`, `create(BoardRequest)` → `BoardDto`, `rename(Long, BoardRequest)` → `BoardDto`, `delete(Long)` — lança `ResourceNotFoundException` quando não encontrado
- [x] T012 [US1] Criar `src/main/java/com/example/board/controller/BoardController.java` mapeando: `GET /api/boards`, `POST /api/boards` (201), `PATCH /api/boards/{id}` (200), `DELETE /api/boards/{id}` (204) — todos os handlers com `@Valid` nos request bodies

**Checkpoint**: US1 completa — `GET/POST/PATCH/DELETE /api/boards` funcionando, nome em branco retorna 400

---

## Fase 4: História de Usuário 2 — Gerenciar Colunas (P2)

**Objetivo**: CRUD completo de colunas dentro de um quadro, com posição automática e ordenação

**Teste Independente**: Com US1 concluída, `GET /api/boards/{boardId}/columns`, `POST /api/boards/{boardId}/columns`, `PATCH /api/columns/{id}` e `DELETE /api/columns/{id}` devem funcionar. Excluir um Board deve remover suas colunas em cascata.

### Implementação

- [x] T013 [P] [US2] Criar `src/main/java/com/example/board/model/BoardColumn.java` com `@Entity`, `@Table(name="columns")`, campos: `id` (Long), `board` (`@ManyToOne @JoinColumn(name="board_id")`), `name` (String, `@NotBlank`), `position` (Integer)
- [x] T014 [P] [US2] Criar `src/main/java/com/example/board/dto/ColumnDto.java` (id, boardId, name, position, cards placeholder como lista vazia) e `src/main/java/com/example/board/dto/ColumnRequest.java` (name com `@NotBlank`, position opcional)
- [x] T015 [US2] Atualizar `src/main/java/com/example/board/model/Board.java` adicionando `@OneToMany(mappedBy="board", cascade=CascadeType.ALL, orphanRemoval=true)` para `List<BoardColumn> columns` (cascade delete de colunas ao excluir board)
- [x] T016 [US2] Criar `src/main/java/com/example/board/repository/ColumnRepository.java` com: `findByBoardOrderByPositionAsc(Board)`, `findMaxPositionByBoardId(@Query)` retornando `Optional<Integer>`
- [x] T017 [US2] Criar `src/main/java/com/example/board/service/ColumnService.java` com métodos: `findByBoard(Long boardId)` → `List<ColumnDto>` ordenada, `create(Long boardId, ColumnRequest)` → `ColumnDto` com position=MAX+1, `update(Long id, ColumnRequest)` → `ColumnDto`, `delete(Long id)` — lança `ResourceNotFoundException` quando board ou coluna não encontrados
- [x] T018 [US2] Criar `src/main/java/com/example/board/controller/ColumnController.java` mapeando: `GET /api/boards/{boardId}/columns` (200), `POST /api/boards/{boardId}/columns` (201), `PATCH /api/columns/{id}` (200), `DELETE /api/columns/{id}` (204)

**Checkpoint**: US2 completa — colunas CRUD funcionando, ordenadas por position, cascade delete do board remove colunas

---

## Fase 5: História de Usuário 3 — Gerenciar Cartões (P3)

**Objetivo**: CRUD completo de cartões, com posição automática; colunas passam a retornar cartões aninhados

**Teste Independente**: Com US1 e US2 concluídas, `POST /api/columns/{columnId}/cards`, `PATCH /api/cards/{id}` (title/description/label/completed) e `DELETE /api/cards/{id}` devem funcionar. `GET /api/boards/{boardId}/columns` deve retornar cada coluna com seus cartões. Excluir uma Column deve remover seus cartões em cascata.

### Implementação

- [x] T019 [P] [US3] Criar `src/main/java/com/example/board/model/Card.java` com `@Entity`, `@Table(name="cards")`, campos: `id` (Long), `column` (`@ManyToOne @JoinColumn(name="column_id")`), `title` (String, `@NotBlank`), `description` (String, nullable), `label` (String, nullable), `position` (Integer), `completed` (boolean, default=false), `createdAt` (LocalDateTime, `@CreationTimestamp`)
- [x] T020 [P] [US3] Criar `src/main/java/com/example/board/dto/CardDto.java` (id, columnId, title, description, label, position, completed, createdAt), `src/main/java/com/example/board/dto/CardRequest.java` (title `@NotBlank`, description?, label?) e `src/main/java/com/example/board/dto/CardUpdateRequest.java` (todos os campos opcionais: title, description, label, position, columnId, completed)
- [x] T021 [US3] Atualizar `src/main/java/com/example/board/model/BoardColumn.java` adicionando `@OneToMany(mappedBy="column", cascade=CascadeType.ALL, orphanRemoval=true)` para `List<Card> cards` (cascade delete de cartões ao excluir coluna)
- [x] T022 [US3] Atualizar `src/main/java/com/example/board/dto/ColumnDto.java` adicionando campo `List<CardDto> cards` (substituir placeholder da Fase 4)
- [x] T023 [US3] Criar `src/main/java/com/example/board/repository/CardRepository.java` com: `findByColumnOrderByPositionAsc(BoardColumn)` e `findMaxPositionByColumnId(@Query)` retornando `Optional<Integer>`
- [x] T024 [US3] Criar `src/main/java/com/example/board/service/CardService.java` com métodos: `create(Long columnId, CardRequest)` → `CardDto` com position=MAX+1, `update(Long id, CardUpdateRequest)` → `CardDto` (atualização parcial dos campos não-nulos), `delete(Long id)` — lança `ResourceNotFoundException` quando coluna ou cartão não encontrados
- [x] T025 [US3] Criar `src/main/java/com/example/board/controller/CardController.java` mapeando: `POST /api/columns/{columnId}/cards` (201), `DELETE /api/cards/{id}` (204) — PATCH /api/cards/{id} será adicionado na US4
- [x] T026 [US3] Atualizar `src/main/java/com/example/board/service/ColumnService.java` no método `findByBoard` para carregar cartões ordenados por position em cada `ColumnDto` (usar `CardRepository.findByColumnOrderByPositionAsc`)

**Checkpoint**: US3 completa — cartões CRUD funcionando, GET colunas retorna cartões aninhados ordenados, cascade delete de coluna remove cartões

---

## Fase 6: História de Usuário 4 — Mover e Reordenar Cartões (P4)

**Objetivo**: Suporte a drag-and-drop — mover cartão entre colunas e reordenar posição via PATCH

**Teste Independente**: Com US1–US3 concluídas, `PATCH /api/cards/{id}` com `columnId` e/ou `position` deve mover o cartão para a coluna destino na posição especificada. O próximo GET das colunas deve refletir a nova posição do cartão.

### Implementação

- [x] T027 [US4] Atualizar `src/main/java/com/example/board/service/CardService.java` no método `update` para suportar `columnId` — quando fornecido, busca a nova `BoardColumn` e reatribui o campo `column` do cartão antes de salvar
- [x] T028 [US4] Adicionar handler `PATCH /api/cards/{id}` (200) ao `src/main/java/com/example/board/controller/CardController.java` recebendo `@Valid @RequestBody CardUpdateRequest` e delegando ao `CardService.update`

**Checkpoint**: US4 completa — drag-and-drop funcional; cartão movido aparece na coluna destino na posição correta no próximo GET

---

## Fase 7: Polimento e Verificação Final

**Objetivo**: Validação end-to-end e ajustes finais de qualidade

- [x] T029 Executar sequência completa do quickstart (`specs/001-board-backend-api/quickstart.md`): criar board → criar colunas → criar cartões → mover cartão → excluir coluna (verificar cascade) → excluir board (verificar cascade)
- [x] T030 [P] Verificar todos os cenários de erro: `POST /api/boards` com `name:""`, `POST /api/columns/{id}/cards` com `title:""` — ambos devem retornar 400 com corpo JSON `{"errors":[{"field":"...","message":"..."}]}`
- [x] T031 [P] Verificar console H2 acessível em `http://localhost:8080/h2-console` com JDBC URL `jdbc:h2:mem:boarddb` e inspecionar tabelas `boards`, `columns`, `cards`

---

## Dependências e Ordem de Execução

### Dependências entre Fases

- **Fase 1 (Setup)**: Sem dependências — pode começar imediatamente
- **Fase 2 (Fundação)**: Depende da conclusão da Fase 1 — **bloqueia todas as histórias**
- **Fase 3 (US1)**: Depende da Fase 2 — sem dependência de outras histórias
- **Fase 4 (US2)**: Depende da Fase 3 (BoardColumn referencia Board que deve existir)
- **Fase 5 (US3)**: Depende da Fase 4 (Card referencia BoardColumn)
- **Fase 6 (US4)**: Depende da Fase 5 (estende CardService e CardController criados em US3)
- **Fase 7 (Polimento)**: Depende de todas as histórias desejadas estarem completas

### Dependências Dentro de Cada História

```
Models + DTOs [P] → Repository → Service → Controller
```

### Oportunidades de Paralelismo

- T003 e T004 (Fase 1): paralelos entre si
- T005, T006, T007 (Fase 2): T006 e T007 paralelos entre si (T005 primeiro pois é usado por T006)
- T008 e T009 (Fase 3): paralelos entre si
- T013 e T014 (Fase 4): paralelos entre si
- T019 e T020 (Fase 5): paralelos entre si
- T030 e T031 (Fase 7): paralelos entre si

---

## Exemplo de Paralelismo: Fase 3 (US1)

```
# Executar em paralelo:
Tarefa: "Criar Board.java em src/main/java/com/example/board/model/Board.java"         [T008]
Tarefa: "Criar BoardDto.java e BoardRequest.java em .../dto/"                           [T009]

# Após T008 e T009 concluídos, sequencial:
T010 → T011 → T012
```

---

## Estratégia de Implementação

### MVP Primeiro (apenas US1)

1. Completar Fase 1: Setup
2. Completar Fase 2: Fundação (CRÍTICO — bloqueia tudo)
3. Completar Fase 3: US1 (Gerenciar Quadros)
4. **PARAR e VALIDAR**: Testar US1 independentemente com curl
5. Demonstrar/entregar MVP

### Entrega Incremental

1. Setup + Fundação → Base pronta
2. US1 → CRUD de Boards funcional → **MVP!**
3. US2 → Colunas funcionando → Estrutura kanban visível
4. US3 → Cartões funcionando → Sistema completo para uso
5. US4 → Drag-and-drop habilitado → Experiência completa
6. Cada etapa adiciona valor sem quebrar o que já funciona

---

## Resumo

| Fase | Descrição | Tarefas | Paralelas |
|------|-----------|---------|-----------|
| 1 — Setup | Projeto Maven + estrutura | T001–T004 (4) | T003, T004 |
| 2 — Fundação | CORS + tratamento de erros | T005–T007 (3) | T006, T007 |
| 3 — US1 (P1) | CRUD de Boards | T008–T012 (5) | T008, T009 |
| 4 — US2 (P2) | CRUD de Colunas | T013–T018 (6) | T013, T014 |
| 5 — US3 (P3) | CRUD de Cartões | T019–T026 (8) | T019, T020 |
| 6 — US4 (P4) | Mover/Reordenar Cartões | T027–T028 (2) | — |
| 7 — Polimento | Verificação final | T029–T031 (3) | T030, T031 |
| **Total** | | **31 tarefas** | |

---

## Notas

- `[P]` = arquivos diferentes, sem dependências — seguro executar em paralelo
- `[USn]` = rastreia tarefa à história de usuário correspondente do spec.md
- Cada história deve ser independentemente testável ao final de sua fase
- Parar em qualquer checkpoint para validar a história antes de avançar
- Evitar: tarefas vagas, conflitos no mesmo arquivo, dependências cruzadas que quebrem a independência das histórias
