# Quickstart: Deploy Contínuo do Backend no Railway

**Branch**: `002-backend-deploy-railway` | **Data**: 2026-04-21

---

## Pré-requisitos

- [ ] Conta no GitHub com repositório `board-backend` existente
- [ ] Conta no Railway (railway.app) com projeto criado
- [ ] Plugin PostgreSQL adicionado ao projeto no Railway dashboard
- [ ] Java 21 e Maven instalados localmente

---

## 1. Configurar segredos no GitHub

Acesse: `Settings → Secrets and variables → Actions → New repository secret`

| Secret | Valor |
|--------|-------|
| `RAILWAY_TOKEN` | Token gerado em Railway → Account Settings → Tokens |
| `GHCR_TOKEN` | PAT GitHub com escopo `write:packages` (Settings → Developer settings → Personal access tokens) |

---

## 2. Configurar variáveis no Railway

No Railway dashboard, selecione o serviço backend e acesse Variables:

| Variável | Valor |
|----------|-------|
| `DATABASE_URL` | Copiado automaticamente do plugin PostgreSQL (começa com `postgresql://`) |
| `SPRING_PROFILES_ACTIVE` | `prod` |

---

## 3. Configurar Railway para usar imagem do GHCR

No Railway dashboard → serviço backend → Settings → Source:
1. Selecionar "Docker Image"
2. Inserir: `ghcr.io/<seu-usuario>/<nome-do-repo>-backend:latest`
3. Salvar

---

## 4. Verificar desenvolvimento local (não deve quebrar)

```bash
./mvnw spring-boot:run
```

Esperado: aplicação sobe em `http://localhost:8080` usando H2.

```bash
curl http://localhost:8080/api/boards
# → [] (lista vazia)

curl http://localhost:8080/actuator/health
# → {"status":"UP"}
```

---

## 5. Disparar o primeiro deploy

```bash
git add .
git commit -m "feat: add CI/CD pipeline and Railway deployment config"
git push origin main
```

Acompanhe em: GitHub → Actions → workflow `Deploy Backend`

Sequência esperada:
```
✅ test         (~2-3 min)
✅ build-and-push  (~3-4 min)
✅ deploy          (~1-2 min)
```

---

## 6. Verificar a aplicação em produção

Após o pipeline completar, aguardar o Railway finalizar o redeploy (~1 min).

```bash
# Substituir pela URL gerada pelo Railway
BASE_URL=https://<projeto>.up.railway.app

curl $BASE_URL/actuator/health
# → {"status":"UP"}

curl $BASE_URL/api/boards
# → []
```

---

## Troubleshooting

### Job `test` falhou
```bash
# Executar testes localmente para diagnosticar
./mvnw test
```

### Job `build-and-push` falhou com erro de autenticação
- Verificar se `GHCR_TOKEN` está configurado corretamente no GitHub Secrets
- Verificar se o PAT tem o escopo `write:packages`

### Job `deploy` falhou
- Verificar se `RAILWAY_TOKEN` está configurado no GitHub Secrets
- Verificar se o nome do serviço no Railway corresponde a `backend`

### Aplicação sobe mas retorna 500 ou falha ao conectar ao banco
- Verificar se `DATABASE_URL` está configurada no Railway (deve começar com `postgresql://`)
- Verificar se `SPRING_PROFILES_ACTIVE=prod` está configurada no Railway
- Verificar logs no Railway dashboard → serviço → Deployments → logs

### Aplicação local parou de funcionar após as alterações
- Garantir que `SPRING_PROFILES_ACTIVE` NÃO está definida no ambiente local
- Executar `./mvnw spring-boot:run` sem variáveis de ambiente adicionais
