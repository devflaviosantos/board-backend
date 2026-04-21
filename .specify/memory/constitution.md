<!--
Relatório de Sincronização
Mudança de versão: 1.0.0 → 1.0.1
Princípios modificados: N/A (tradução para português — sem alterações semânticas)
Seções adicionadas: N/A
Seções removidas: N/A
Templates que requerem atualização:
  - .specify/templates/plan-template.md ✅ sem alterações necessárias
  - .specify/templates/spec-template.md ✅ sem alterações necessárias
  - .specify/templates/tasks-template.md ✅ sem alterações necessárias
  - README.md ✅ já alinhado com a constituição
TODOs pendentes: Nenhum
-->

# Constituição — Board Backend API

## Princípios Fundamentais

### I. Arquitetura MVC em Camadas

O sistema DEVE seguir a estrutura Controller → Service → Repository.
Controllers tratam exclusivamente de preocupações HTTP; a lógica de negócio
pertence aos Services; o acesso a dados pertence aos Repositories. Nenhuma
camada DEVE ignorar outra.

DTOs DEVEM ser usados na fronteira do controller — entidades JPA NÃO DEVEM
ser expostas diretamente em payloads de requisição ou resposta. DTOs de
requisição e resposta DEVEM ser tipos distintos.

**Justificativa**: A separação de responsabilidades permite testes unitários
independentes por camada e possibilita a troca da estratégia de persistência
(ex.: H2 → PostgreSQL) sem alterar a lógica de negócio ou os contratos da API.

### II. Cobertura de Testes em Todas as Camadas

Todos os Services DEVEM ter testes unitários com JUnit 5 + Mockito. Todos os
Controllers DEVEM ter testes de integração com @SpringBootTest + TestRestTemplate.
A cobertura do caminho principal e dos principais cenários de erro é OBRIGATÓRIA
antes de uma funcionalidade ser considerada concluída.

**Justificativa**: Este projeto é uma API REST; endpoints sem testes quebram
silenciosamente o contrato com o frontend. Testes unitários sozinhos não
detectam problemas de configuração do Spring ou serialização JSON — ambas as
camadas de teste são obrigatórias.

### III. Integridade em Cascata dos Dados

Relacionamentos pai-filho DEVEM declarar CascadeType.ALL com
orphanRemoval=true. Deletar um Board DEVE cascatear para suas Colunas e Cards.
Deletar uma Coluna DEVE cascatear para seus Cards. Registros órfãos não são
permitidos.

**Justificativa**: Uma ferramenta Kanban pessoal espera operações destrutivas
intuitivas. Registros órfãos em um banco em memória corrompem as listagens e
desperdiçam recursos.

### IV. Posicionamento Controlado pelo Frontend

O backend DEVE persistir os valores de posição exatamente como recebidos do
cliente. O backend NÃO DEVE reordenar, normalizar ou reconciliar posições
autonomamente. Novos itens adicionados sem posição explícita DEVEM receber
MAX(position) + 1, calculado via query derivada JPA, evitando round-trips
desnecessários ao cliente.

**Justificativa**: A reordenação por drag-and-drop é responsabilidade do
frontend. Manter o backend como simples armazenador de posições evita lógicas
de reordenação conflitantes e mantém o contrato da API estável.

### V. Simplicidade e Escopo MVP

O projeto DEVE permanecer single-user sem camada de autenticação. O H2 em
memória é o padrão obrigatório; o suporte ao PostgreSQL DEVE ser alcançável
exclusivamente via mudanças em application.yml — nenhuma alteração de código
é permitida. Nenhuma funcionalidade DEVE ser adicionada além do escopo de
CRUD + reordenação definido na spec sem um aditamento formal.

**Justificativa**: Este é um MVP pessoal. Complexidade prematura (autenticação,
múltiplos usuários, framework de migrações) atrasaria a entrega e adicionaria
carga de manutenção sem valor imediato.

## Restrições Técnicas

- **Linguagem**: Java 21 — versões mais antigas da JVM não são permitidas.
- **Framework**: Spring Boot 3.3 com Spring Data JPA e Spring Validation (Bean Validation 3).
- **Build**: Maven 3.8+; o Maven wrapper (`./mvnw`) DEVE ser versionado no repositório.
- **Armazenamento**: H2 em memória (padrão); troca para PostgreSQL somente via `application.yml`.
- **CORS**: Requisições de `http://localhost:4200` DEVEM ser aceitas; demais origens
  são bloqueadas salvo adição explícita em `CorsConfig`.
- **Tratamento de erros**: Todos os erros de validação e negócio DEVEM ser tratados
  pelo `GlobalExceptionHandler` (@ControllerAdvice); controllers NÃO DEVEM
  propagar exceções brutas ao cliente.
- **Pacote raiz**: `com.example.board` — consistente em todos os arquivos-fonte.

## Fluxo de Desenvolvimento

- Toda funcionalidade DEVE ter uma spec em `specs/###-feature-name/spec.md`
  antes do início da implementação.
- O gate de Verificação de Constituição no plan.md DEVE ser aprovado antes da
  pesquisa da Fase 0 e DEVE ser reverificado após o design da Fase 1.
- As tarefas DEVEM ser geradas pelo `/speckit-tasks` e rastreadas em `tasks.md`.
- Cada tarefa concluída DEVE ser commitada com uma mensagem descritiva.
- O `quickstart.md` de cada spec DEVE ser validado de ponta a ponta antes de
  a funcionalidade ser marcada como concluída.

## Governança

Esta constituição substitui todos os acordos informais e notas de planejamento
anteriores. Alterações requerem:

1. Uma justificativa escrita explicando por que um princípio não se aplica mais
   ou precisa ser revisado.
2. Um incremento de versão semântica (veja a política abaixo).
3. Atualização de todos os templates e documentações de runtime afetados.

**Política de versionamento**:
- MAJOR: Remoção de princípio ou redefinição incompatível com versões anteriores.
- MINOR: Novo princípio ou expansão material de uma diretriz.
- PATCH: Esclarecimentos, correções de redação, refinamentos não semânticos.

Todos os PRs que tocam arquitetura, entidades ou contratos de API DEVEM
verificar conformidade com esta constituição antes do merge. Exceções de
complexidade DEVEM ser documentadas na tabela de Rastreamento de Complexidade
do plan.md com justificativa.

**Versão**: 1.0.1 | **Ratificado em**: 2026-04-20 | **Última alteração**: 2026-04-21
