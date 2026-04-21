# Plano de Implementação: Backend do Quadro Kanban Pessoal

**Branch**: `001-board-backend-api` | **Data**: 2026-04-20 | **Spec**: [spec.md](spec.md)
**Input**: Especificação de funcionalidade em `specs/001-board-backend-api/spec.md`

## Resumo

API REST para gerenciamento pessoal de quadros kanban (estilo Trello). O sistema suporta CRUD completo de Quadros, Colunas e Cartões, com reordenação por posição numérica controlada pelo frontend (drag-and-drop). Implementado com Java 21, Spring Boot 3.3, Spring Data JPA e banco H2 in-memory.

## Contexto Técnico

**Linguagem/Versão**: Java 21  
**Dependências Principais**: Spring Boot 3.3, Spring Data JPA, Spring Validation, H2  
**Armazenamento**: H2 in-memory (MVP — fácil substituição por PostgreSQL via configuração)  
**Testes**: JUnit 5 + Mockito + @SpringBootTest + TestRestTemplate  
**Plataforma Alvo**: Servidor local Linux  
**Tipo de Projeto**: web-service (API REST)  
**Metas de Performance**: Padrão para aplicação web pessoal (sem carga concorrente)  
**Restrições**: CORS permitido para `http://localhost:4200`; dados não precisam persistir entre reinicializações  
**Escopo**: Single user, MVP  

## Verificação de Constituição

A constituição do projeto está em estágio de template (não customizada). Nenhuma restrição ativa aplicável.

**Gates**:
- Sem violações de constituição identificadas
- Complexidade justificada: padrão MVC (Controller → Service → Repository) adequado para API REST CRUD

## Estrutura do Projeto

### Documentação (esta funcionalidade)

```
specs/001-board-backend-api/
├── plan.md           ← este arquivo
├── spec.md           ← especificação da funcionalidade
├── research.md       ← decisões técnicas (Fase 0)
├── data-model.md     ← modelo de entidades e DTOs (Fase 1)
├── quickstart.md     ← como executar e testar (Fase 1)
├── contracts/
│   └── api.md        ← contrato completo da API REST (Fase 1)
└── tasks.md          ← gerado por /speckit-tasks (ainda não criado)
```

### Código Fonte (raiz do repositório)

```
src/
├── main/
│   ├── java/com/example/board/
│   │   ├── BoardApplication.java
│   │   ├── config/
│   │   │   ├── CorsConfig.java              ← CORS para localhost:4200
│   │   │   └── GlobalExceptionHandler.java  ← tratamento global de validação
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
│   │   │   ├── BoardColumn.java             ← @Table(name="columns")
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

**Decisão de Estrutura**: Projeto único Maven com pacote base `com.example.board`. Padrão MVC clássico (Controller → Service → Repository) com DTOs separados das entidades JPA. Sem módulos ou subprojetos — escopo MVP simples.

## Principais Decisões de Design

| Decisão | Escolha | Ver |
|---------|---------|-----|
| Nomenclatura da entidade coluna | Classe `BoardColumn`, tabela `columns` | [research.md](research.md#decisão-1) |
| Gerenciamento de posição | Frontend-driven, backend persiste como recebido | [research.md](research.md#decisão-2) |
| Cascade delete | `CascadeType.ALL` + `orphanRemoval=true` | [research.md](research.md#decisão-3) |
| DTOs | Separados das entidades, requests e responses distintos | [research.md](research.md#decisão-4) |
| Tratamento de erros | `@ControllerAdvice` + `GlobalExceptionHandler` | [research.md](research.md#decisão-5) |
| Banco de dados | H2 in-memory com console em `/h2-console` | [research.md](research.md#decisão-6) |
| CORS | `WebMvcConfigurer` global para `localhost:4200` | [research.md](research.md#decisão-7) |
| Auto-posição | `MAX(position) + 1` via query JPA derivada | [research.md](research.md#decisão-9) |

## Artefatos Gerados

| Artefato | Fase | Descrição |
|----------|------|-----------|
| [research.md](research.md) | 0 | Decisões técnicas e alternativas consideradas |
| [data-model.md](data-model.md) | 1 | Entidades, campos, relacionamentos e DTOs |
| [contracts/api.md](contracts/api.md) | 1 | Contrato completo da API REST com exemplos |
| [quickstart.md](quickstart.md) | 1 | Setup, dependências, execução e verificação |

## Próximo Passo

```
/speckit-tasks
```
