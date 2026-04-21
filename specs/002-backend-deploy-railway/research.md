# Pesquisa Técnica: Deploy Contínuo do Backend no Railway

**Branch**: `002-backend-deploy-railway` | **Data**: 2026-04-21

---

## Decisão 1: Localização do Dockerfile

**Decisão**: Dockerfile na raiz do repositório (`./Dockerfile`)

**Rationale**: O repositório `board-backend` contém exclusivamente o backend
Java — não há subdiretório `backend/`. Colocar o Dockerfile na raiz é o padrão
para projetos Spring Boot single-module e é a configuração que o Railway espera
por padrão ao usar `railway.json`.

**Alternativas consideradas**:
- `backend/Dockerfile`: Faria sentido em monorepo com frontend no mesmo repo;
  desnecessário aqui pois o frontend está em repositório separado (`board-frontend`).

---

## Decisão 2: Estratégia de build multistage

**Decisão**: Stage `build` com `maven:3.9-eclipse-temurin-21` + stage `runtime`
com `eclipse-temurin:21-jre-alpine`

**Rationale**: A imagem Alpine JRE reduz o tamanho final (~180 MB vs ~550 MB
da imagem JDK completa). O stage de build compila e empacota o JAR sem incluir
o Maven e as dependências de compilação na imagem final.

**Alternativas consideradas**:
- Imagem única com JDK: Mais simples, mas imagem final excessivamente grande.
- Buildpacks (Cloud Native Buildpacks): Mais automático, mas menos transparente
  e requer configuração adicional com Maven wrapper.

---

## Decisão 3: Separação de perfis Spring (dev vs prod)

**Decisão**: Manter `application.yml` para desenvolvimento local (H2) e criar
`application-prod.yml` para produção (PostgreSQL). Perfil ativado via
`SPRING_PROFILES_ACTIVE=prod` (variável de ambiente no Railway).

**Rationale**: Abordagem nativa do Spring Boot — sem código adicional. O
perfil `default` continua funcionando para `./mvnw spring-boot:run` sem
nenhuma variável de ambiente. O perfil `prod` é isolado e não interfere no
desenvolvimento local.

**Alternativas consideradas**:
- Um único `application.yml` com condicionais: Mais complexo, propenso a erro,
  dificulta a revisão de segurança.
- Variáveis de ambiente sobrescrevendo tudo: Funciona mas não documenta as
  diferenças entre ambientes de forma clara.

---

## Decisão 4: Leitura do DATABASE_URL no Spring Boot

**Decisão**: `application-prod.yml` usa `jdbc:${DATABASE_URL}` para a URL
do datasource, sem username/password separados.

**Rationale**: O Railway PostgreSQL plugin gera `DATABASE_URL` no formato
`postgresql://user:pass@host:5432/dbname`. Prefixando com `jdbc:` obtemos
`jdbc:postgresql://user:pass@host:5432/dbname` — formato suportado pelo driver
JDBC PostgreSQL (as credenciais embarcadas na URL são aceitas pelo driver).
Isso minimiza o número de variáveis de ambiente que precisam ser configuradas.

**Alternativas consideradas**:
- `PGHOST`, `PGPORT`, `PGUSER`, `PGPASSWORD` separados: Mais verboso, requer
  mais configuração manual no Railway dashboard.
- `SPRING_DATASOURCE_URL` direto: Funcionaria, mas o Railway não gera esta
  variável automaticamente — exige configuração manual adicional.

---

## Decisão 5: Dependências a adicionar no pom.xml

**Decisão**: Adicionar `spring-boot-starter-actuator` e `postgresql` driver.

**Rationale**:
- **Actuator**: Obrigatório para o healthcheck do Railway (`/actuator/health`).
  Sem ele, o Railway não consegue verificar se o serviço está saudável e pode
  reiniciar o container desnecessariamente.
- **postgresql**: O driver H2 já está presente para dev. O driver PostgreSQL
  precisa ser adicionado com escopo `runtime` para produção. Spring Boot
  seleciona automaticamente o driver correto com base na URL do datasource.

---

## Decisão 6: Path filter do GitHub Actions workflow

**Decisão**: Trigger em `push` na branch `main` com paths `src/**`, `pom.xml`,
`Dockerfile`, `railway.json` e `.github/workflows/deploy-backend.yml`.

**Rationale**: O repositório `board-backend` contém apenas o backend — não há
subdiretório `backend/`. O path filter `backend/**` especificado inicialmente
não corresponde à estrutura real. Os paths escolhidos cobrem exatamente os
arquivos que, quando alterados, devem disparar o pipeline. Arquivos de
documentação (`specs/`, `README.md`) não disparam deploy desnecessário.

---

## Decisão 7: Mecanismo de deploy para o Railway

**Decisão**: Job `deploy` usa `railway-app/railway-action@v1` para acionar
o redeploy do serviço Railway após a imagem ser publicada no GHCR.

**Rationale**: O Railway pode ser configurado no dashboard para usar uma imagem
de um registro externo (GHCR). O `railway-app/railway-action@v1` usa a CLI do
Railway internamente para acionar o redeploy. A variável `RAILWAY_SERVICE`
identifica qual serviço redeploy.

**Pré-requisito de configuração manual no Railway dashboard**:
1. Definir a fonte da imagem como `ghcr.io/<owner>/<repo>-backend:latest`
2. Configurar `DATABASE_URL` e `SPRING_PROFILES_ACTIVE=prod`

**Alternativas consideradas**:
- Railway auto-pull via webhook: Requer plano pago ou configuração complexa de
  webhooks entre GHCR e Railway.
- Railway CLI `railway redeploy` direto: Equivalente ao action, mas sem a
  abstração de retry/error handling que o action fornece.

---

## Decisão 8: Porta do servidor em produção

**Decisão**: `application-prod.yml` define `server.port: ${PORT:8080}` para
respeitar a variável `PORT` injetada pelo Railway.

**Rationale**: O Railway injeta a variável `PORT` dinamicamente. Usar
`${PORT:8080}` mantém 8080 como fallback para execução local com o perfil prod
ativo, sem quebrar o desenvolvimento.

---

## Artefatos a criar/modificar

| Arquivo | Ação | Descrição |
|---------|------|-----------|
| `Dockerfile` | CRIAR | Multistage build Maven → JRE Alpine |
| `railway.json` | CRIAR | Config Railway: Dockerfile path + healthcheck |
| `pom.xml` | MODIFICAR | Adicionar actuator + postgresql driver |
| `src/main/resources/application.yml` | MODIFICAR | Marcar explicitamente como perfil dev |
| `src/main/resources/application-prod.yml` | CRIAR | Config PostgreSQL para produção |
| `.github/workflows/deploy-backend.yml` | CRIAR | Pipeline CI/CD 3 jobs |
