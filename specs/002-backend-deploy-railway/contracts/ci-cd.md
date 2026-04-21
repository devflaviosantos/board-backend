# Contrato do Pipeline CI/CD

**Branch**: `002-backend-deploy-railway` | **Data**: 2026-04-21

---

## Visão Geral

O pipeline é definido em `.github/workflows/deploy-backend.yml` e composto
por três jobs sequenciais com dependências explícitas.

```
push → main (paths filtrados)
         │
         ▼
      [test]
         │ sucesso
         ▼
  [build-and-push]
         │ sucesso
         ▼
      [deploy]
```

---

## Triggers

| Evento | Branch | Paths | Comportamento |
|--------|--------|-------|---------------|
| `push` | `main` | `src/**`, `pom.xml`, `Dockerfile`, `railway.json`, `.github/workflows/deploy-backend.yml` | Dispara o pipeline completo |
| Qualquer outro push | qualquer | qualquer fora dos paths | Pipeline não disparado |

---

## Job: test

**Objetivo**: Garantir que todos os testes passam antes de qualquer artefato
ser publicado.

**Ambiente**: `ubuntu-latest`

**Passos**:
1. `actions/checkout@v4` — faz checkout do código
2. `actions/setup-java@v4` — configura JDK 21 (distribuição Temurin)
3. `actions/cache@v4` — cache do diretório `~/.m2` com chave baseada no hash do `pom.xml`
4. `mvn test` — executa todos os testes

**Contrato de saída**:
- Exit code 0 → job passa; `build-and-push` pode iniciar
- Exit code ≠ 0 → pipeline falha; nenhum artefato é publicado; deploy não ocorre

---

## Job: build-and-push

**Objetivo**: Construir a imagem Docker e publicar no GitHub Container Registry.

**Dependência**: `needs: [test]`

**Ambiente**: `ubuntu-latest`

**Passos**:
1. `actions/checkout@v4`
2. `docker/login-action@v3` — autenticação no `ghcr.io` com `GHCR_TOKEN`
3. `docker/build-push-action@v5` — build multistage e push

**Tag da imagem publicada**:
```
ghcr.io/<github_owner>/<repo_name>-backend:latest
```

**Contrato de saída**:
- Imagem publicada com sucesso → `deploy` pode iniciar
- Falha no build ou no push → pipeline para; Railway não é acionado

---

## Job: deploy

**Objetivo**: Acionar o redeploy do serviço no Railway.

**Dependência**: `needs: [build-and-push]`

**Ambiente**: `ubuntu-latest`

**Passos**:
1. `railway-app/railway-action@v1` com `RAILWAY_TOKEN` e `RAILWAY_SERVICE=backend`

**Pré-requisito no Railway dashboard**:
- Serviço `backend` configurado para usar a imagem de `ghcr.io`
- Variáveis de ambiente configuradas: `DATABASE_URL`, `SPRING_PROFILES_ACTIVE=prod`

**Contrato de saída**:
- Redeploy acionado com sucesso → aplicação sobe com nova imagem no Railway
- Falha → pipeline reporta erro; serviço anterior permanece ativo (sem downtime)

---

## Segredos necessários

### GitHub (Settings → Secrets and variables → Actions)

| Secret | Descrição | Como obter |
|--------|-----------|-----------|
| `RAILWAY_TOKEN` | Token de autenticação Railway | Railway dashboard → Account Settings → Tokens |
| `GHCR_TOKEN` | Personal Access Token GitHub | GitHub → Settings → Developer settings → PAT → escopo `write:packages` |

### Railway (Dashboard → Service → Variables)

| Variável | Valor | Origem |
|----------|-------|--------|
| `DATABASE_URL` | `postgresql://user:pass@host:5432/dbname` | Gerado automaticamente pelo plugin PostgreSQL |
| `SPRING_PROFILES_ACTIVE` | `prod` | Configurar manualmente |

---

## Healthcheck de produção

| Endpoint | Método | Resposta esperada | Timeout |
|----------|--------|-------------------|---------|
| `/actuator/health` | GET | `{"status":"UP"}` HTTP 200 | 30s |
| `/api/boards` | GET | `[]` ou lista de boards HTTP 200 | — |

---

## Garantias do contrato

1. **Nenhum deploy com testes falhando** — o job `build-and-push` só executa
   após `test` terminar com sucesso.
2. **Nenhuma credencial no código** — todos os segredos são injetados via
   variáveis de ambiente de plataforma.
3. **Rollback implícito** — se o job `deploy` falhar, o Railway mantém a versão
   anterior ativa; não há downtime forçado.
4. **Isolamento de ambiente** — o perfil `prod` é ativado exclusivamente via
   `SPRING_PROFILES_ACTIVE`; o perfil dev permanece funcional sem alterações.
