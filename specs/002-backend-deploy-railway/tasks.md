---
description: "Lista de tarefas para deploy contínuo do backend no Railway"
---

# Tarefas: Deploy Contínuo do Backend no Railway

**Input**: Documentos de design em `specs/002-backend-deploy-railway/`
**Pré-requisitos**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/ ✅

**Testes**: Não solicitados explicitamente — sem tarefas de TDD.

**Organização**: Tarefas agrupadas por história de usuário para permitir
implementação e verificação independentes de cada história.

## Formato: `[ID] [P?] [Story?] Descrição`

- **[P]**: Pode rodar em paralelo (arquivos diferentes, sem dependências)
- **[Story]**: A qual história de usuário pertence (US1, US2, US3)
- Caminhos de arquivo exatos incluídos nas descrições

---

## Fase 1: Setup (Infraestrutura Compartilhada)

**Objetivo**: Adicionar dependências que habilitam healthcheck e conexão com
PostgreSQL — bloqueia todas as histórias de usuário.

- [x] T001 Adicionar `spring-boot-starter-actuator` e dependência `postgresql` (escopo `runtime`) em `pom.xml`

---

## Fase 2: Fundacional (Pré-requisitos Bloqueadores)

**Objetivo**: Configuração de perfis Spring que separa dev (H2) de prod
(PostgreSQL) — deve ser concluída antes das histórias de usuário.

**⚠️ CRÍTICO**: Nenhuma história de usuário pode ser implementada sem esta fase.

- [x] T002 Modificar `src/main/resources/application.yml` — adicionar `spring.config.activate.on-profile: "!prod"` e alterar `server.port` para `${PORT:8080}`
- [x] T003 [P] Criar `src/main/resources/application-prod.yml` — datasource com `jdbc:${DATABASE_URL}`, `ddl-auto: update`, H2 console desativado, `show-sql: false`, `server.port: ${PORT:8080}`, logs em INFO

**Checkpoint**: Perfis dev/prod configurados — histórias de usuário podem iniciar.

---

## Fase 3: História de Usuário 1 — Pipeline CI/CD Ativado por Push (P1) 🎯 MVP

**Objetivo**: Push na `main` dispara automaticamente: testes → build Docker
→ push GHCR → redeploy Railway.

**Teste Independente**: Fazer push na branch `main` e observar o workflow
`deploy-backend.yml` no GitHub Actions completar os três jobs com sucesso,
seguido de `GET /api/boards` retornando HTTP 200 na URL do Railway.

### Implementação da História de Usuário 1

- [x] T004 [P] [US1] Criar `Dockerfile` na raiz do repositório — multistage: stage `build` usa `maven:3.9-eclipse-temurin-21` (copia `pom.xml` + `src/`, roda `mvn package -DskipTests`); stage `runtime` usa `eclipse-temurin:21-jre-alpine` (copia JAR para `/app/app.jar`, EXPOSE 8080, ENTRYPOINT `["java","-jar","app.jar"]`)
- [x] T005 [P] [US1] Criar `railway.json` na raiz — `dockerfilePath: "Dockerfile"`, `healthcheckPath: "/actuator/health"`, `healthcheckTimeout: 30`, `restartPolicyType: "ON_FAILURE"`
- [x] T006 [US1] Criar `.github/workflows/deploy-backend.yml` — trigger `push` em `main` com paths `src/**`, `pom.xml`, `Dockerfile`, `railway.json`, `.github/workflows/deploy-backend.yml`; job `test` (checkout + setup-java 21 Temurin + cache ~/.m2 + `mvn test`); job `build-and-push` (needs: test; login ghcr.io com GHCR_TOKEN; build+push `ghcr.io/${{ github.repository_owner }}/${{ github.event.repository.name }}-backend:latest`); job `deploy` (needs: build-and-push; `railway-app/railway-action@v1` com RAILWAY_TOKEN e RAILWAY_SERVICE=backend)

**Checkpoint**: Push na `main` → pipeline completo → app disponível no Railway.

---

## Fase 4: História de Usuário 2 — Proteção Contra Regressões (P2)

**Objetivo**: Confirmar que testes falhando impedem build e deploy. Esta
história é satisfeita pelo design do workflow (dependência `needs: [test]`) —
a fase consiste em verificar que a configuração está correta.

**Teste Independente**: Introduzir um teste falhando, fazer push, confirmar
que apenas o job `test` executa e os demais ficam bloqueados.

### Verificação da História de Usuário 2

- [x] T007 [US2] Revisar `.github/workflows/deploy-backend.yml` e confirmar que os jobs `build-and-push` e `deploy` declaram `needs: [test]` — garantindo que falha em testes bloqueia 100% dos deploys downstream

**Checkpoint**: História 2 satisfeita pela configuração de dependência dos jobs.

---

## Fase 5: História de Usuário 3 — Aplicação Rodando com PostgreSQL em Produção (P3)

**Objetivo**: Após deploy, a aplicação conecta ao PostgreSQL do Railway (não
H2), e dados persistem entre reinicializações.

**Teste Independente**: Acessar `GET /actuator/health` retorna `{"status":"UP"}`;
criar um board via `POST /api/boards`, reiniciar o serviço no Railway e
verificar que o board persiste via `GET /api/boards`.

### Verificação da História de Usuário 3

- [x] T008 [P] [US3] Verificar que `pom.xml` contém `spring-boot-starter-actuator` (T001) e driver `postgresql` com escopo `runtime` (T001)
- [x] T009 [US3] Verificar que `src/main/resources/application-prod.yml` (T003) contém: `url: "jdbc:${DATABASE_URL}"`, `driver-class-name: org.postgresql.Driver`, `ddl-auto: update`, `h2.console.enabled: false`, `server.port: ${PORT:8080}`

**Checkpoint**: Todas as histórias de usuário independentemente funcionais.

---

## Fase Final: Polish e Preocupações Transversais

**Objetivo**: Validação end-to-end e garantias de segurança.

- [x] T010 Executar `./mvnw test` localmente para confirmar que todos os testes passam com as novas dependências adicionadas em T001
- [x] T011 Verificar `.gitignore` — confirmar que nenhum arquivo com credenciais (`.env`, `application-local.yml`, `secrets.*`) está rastreado pelo Git
- [ ] T012 Validar os passos do `specs/002-backend-deploy-railway/quickstart.md` end-to-end: configurar segredos GitHub, configurar variáveis Railway, fazer push, verificar pipeline, verificar `GET /actuator/health` e `GET /api/boards` em produção

---

## Dependências e Ordem de Execução

### Dependências entre Fases

- **Setup (Fase 1)**: Sem dependências — pode iniciar imediatamente
- **Fundacional (Fase 2)**: Depende da conclusão do Setup — bloqueia todas as histórias
- **Histórias de Usuário (Fases 3, 4, 5)**: Dependem da fase Fundacional
  - Fase 4 depende da conclusão da Fase 3 (T006 deve existir para T007 verificar)
  - Fase 5 depende da conclusão da Fase 2 (T003 deve existir para T009 verificar)
- **Polish (Fase Final)**: Depende de todas as histórias de usuário desejadas

### Dependências entre Histórias

- **US1 (P1)**: Pode iniciar após Fundacional — sem dependências em outras histórias
- **US2 (P2)**: Depende da US1 — verifica a configuração criada por T006
- **US3 (P3)**: Pode iniciar em paralelo com US1 após Fundacional — verifica T001 + T003

### Dentro de Cada História

- T004 e T005 podem rodar em paralelo (Dockerfile e railway.json são independentes)
- T006 depende de T004 e T005 estarem prontos antes do primeiro push real
- T008 e T009 podem rodar em paralelo (verificações independentes)

### Oportunidades de Paralelismo

- T002 e T003 podem rodar em paralelo (arquivos distintos)
- T004 e T005 podem rodar em paralelo (arquivos distintos)
- T008 e T009 podem rodar em paralelo

---

## Exemplo de Paralelismo: Fase Fundacional

```bash
# Executar simultaneamente:
Tarefa: "Modificar application.yml — perfil dev explícito" (T002)
Tarefa: "Criar application-prod.yml — config PostgreSQL" (T003)
```

## Exemplo de Paralelismo: Fase US1

```bash
# Executar simultaneamente:
Tarefa: "Criar Dockerfile multistage na raiz" (T004)
Tarefa: "Criar railway.json na raiz" (T005)

# Após T004 e T005:
Tarefa: "Criar .github/workflows/deploy-backend.yml" (T006)
```

---

## Estratégia de Implementação

### MVP Primeiro (apenas História de Usuário 1)

1. Concluir Fase 1: Setup (T001)
2. Concluir Fase 2: Fundacional (T002, T003) — CRÍTICO
3. Concluir Fase 3: US1 (T004, T005, T006)
4. **PARAR E VALIDAR**: Fazer push e testar o pipeline end-to-end
5. Deploy funcionando → continuar com US2 e US3

### Entrega Incremental

1. Setup + Fundacional → base pronta
2. US1 → pipeline CI/CD funcionando → Validar → **MVP de deploy!**
3. US2 → proteção contra regressões verificada → Validar
4. US3 → produção com PostgreSQL confirmada → Validar
5. Polish → pipeline robusto e documentado

---

## Notas

- `[P]` = arquivos diferentes, sem dependências entre si
- Label `[Story]` mapeia tarefa para rastreabilidade na história de usuário
- Cada história de usuário é independentemente completável e testável
- Commitar após cada tarefa ou grupo lógico
- Parar em qualquer checkpoint para validar a história independentemente
- Evitar: tarefas vagas, conflitos no mesmo arquivo, dependências cross-story
  que quebram independência
