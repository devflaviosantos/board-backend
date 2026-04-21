# Pesquisa e Decisões: Backend do Quadro Kanban Pessoal

**Branch**: `001-board-backend-api` | **Data**: 2026-04-20

## Decisão 1: Nomenclatura da Entidade Coluna

**Decisão**: Usar `BoardColumn` como nome da classe Java, com `@Table(name="columns")`

**Justificativa**: `Column` é uma palavra reservada no SQL e causa conflito em alguns dialetos de banco. Usar `BoardColumn` como nome da classe Java elimina a ambiguidade sem impactar o schema ou a API.

**Alternativas consideradas**:
- Renomear a tabela para `board_columns` — rejeitado para manter o schema limpo e semântico
- Usar `Column` com escape SQL — arriscado e dependente de dialeto

---

## Decisão 2: Gerenciamento de Posição (Reordenação / Movimentação)

**Decisão**: O backend persiste o valor de `position` exatamente como enviado pelo frontend, sem reordenação automática de outros itens.

**Justificativa**: O spec define explicitamente que o frontend (drag-and-drop) é responsável por calcular as novas posições. Lógica de reordenação no servidor adicionaria complexidade desnecessária para uso pessoal.

**Alternativas consideradas**:
- Reordenação automática com compactação (gap filling) — rejeitado: complexidade desnecessária para MVP
- Usar número fracionário (float) para posição (Lexorank simplificado) — rejeitado: overkill para uso pessoal

---

## Decisão 3: Estratégia de Cascade Delete

**Decisão**: Usar `CascadeType.ALL` + `orphanRemoval = true` nas associações `Board → BoardColumn` e `BoardColumn → Card`.

**Justificativa**: Garante remoção automática em cascata ao excluir um Board ou uma Column, alinhado com os requisitos RF-004 e RF-008 sem necessidade de lógica manual.

**Alternativas consideradas**:
- `@OneToMany` sem cascade + deleção manual nos services — rejeitado: propenso a erros e mais verboso
- `ON DELETE CASCADE` no schema SQL — rejeitado: configuração de DDL manual que conflita com `ddl-auto: create-drop`

---

## Decisão 4: Estratégia de DTOs

**Decisão**: Criar DTOs separados para requests e responses, sem expor entidades JPA diretamente.

**Justificativa**: Separa o modelo de persistência do contrato da API, evitando problemas de serialização circular (Board → Column → Board) e facilitando evolução futura.

**Alternativas consideradas**:
- Serializar entidades JPA diretamente com `@JsonIgnore` — rejeitado: risco de referências circulares e acoplamento entre camadas
- Spring HATEOAS — rejeitado: overkill para API pessoal simples

---

## Decisão 5: Tratamento de Erros de Validação

**Decisão**: Usar `@ControllerAdvice` com um `GlobalExceptionHandler` que captura `MethodArgumentNotValidException` e retorna `400 Bad Request` com mensagem descritiva.

**Justificativa**: Centraliza o tratamento de erros de validação sem repetir código nos controllers. A mensagem de erro deve identificar o campo inválido (ex: "name: não pode ser em branco").

**Alternativas consideradas**:
- Tratar exceções em cada controller manualmente — rejeitado: repetitivo
- `@ResponseStatus` nas exceções customizadas — complementar, não substituto para validação de bean

---

## Decisão 6: Configuração do H2 e JPA

**Decisão**: H2 in-memory com console habilitado em `/h2-console`, DDL auto `create-drop`, e `show-sql: true` para debugging.

**Justificativa**: Configuração ideal para MVP local: banco criado na inicialização, destruído ao encerrar, console web disponível para inspeção. Fácil substituição por PostgreSQL via configuração de `datasource`.

**Alternativas consideradas**:
- H2 file-based (`jdbc:h2:file:./boarddb`) — rejeitado: persistência entre restarts fora do escopo v1
- PostgreSQL direto — rejeitado: requer instalação adicional, contra o escopo do MVP

---

## Decisão 7: Configuração de CORS

**Decisão**: Implementar `CorsConfig` via `WebMvcConfigurer` permitindo origem `http://localhost:4200` com todos os métodos HTTP e headers.

**Justificativa**: Configuração centralizada e declarativa, aplicada a todos os endpoints sem anotação por controller.

**Alternativas consideradas**:
- `@CrossOrigin` por controller — rejeitado: duplicação de configuração
- Gateway/proxy reverso — rejeitado: complexidade desnecessária para desenvolvimento local

---

## Decisão 8: Estratégia de Testes

**Decisão**: Testes de integração com `@SpringBootTest` + `TestRestTemplate` para controllers, e testes unitários com Mockito para services.

**Justificativa**: Testes de integração validam o comportamento end-to-end (HTTP → DB → resposta) com H2 em memória. Testes unitários de service validam a lógica de negócio isolada.

**Alternativas consideradas**:
- `@WebMvcTest` (MockMvc) apenas — rejeitado: não valida a camada JPA
- Apenas testes de integração — rejeitado: mais lentos para lógica de negócio unitária

---

## Decisão 9: Auto-atribuição de Posição

**Decisão**: Ao criar uma Column ou Card, a posição é calculada como `MAX(position) + 1` dentro do pai (board ou coluna) via query JPA derivada.

**Justificativa**: Simples e sem race conditions para uso single-user. Se não há itens no pai, position = 1.

**Alternativas consideradas**:
- Contar itens + 1 — rejeitado: incorreto se houver gaps de posição após reordenações
- UUID como posição — rejeitado: incompatível com ordenação numérica esperada pelo frontend

---

## Resumo das Tecnologias

| Componente       | Escolha                         |
|------------------|---------------------------------|
| Linguagem        | Java 21                         |
| Framework        | Spring Boot 3.3                 |
| Persistência     | Spring Data JPA + Hibernate     |
| Banco de dados   | H2 in-memory (MVP)              |
| Validação        | Spring Validation (Bean Validation 3) |
| Build            | Maven (padrão Spring Initializr) |
| Testes           | JUnit 5 + Mockito + SpringBootTest |
| CORS             | Spring WebMvcConfigurer         |
