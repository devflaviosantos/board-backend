# Especificação de Funcionalidade: Deploy Contínuo do Backend no Railway

**Branch da Funcionalidade**: `002-backend-deploy-railway`
**Criado em**: 2026-04-21
**Status**: Draft

## Cenários de Usuário e Testes *(obrigatório)*

### História de Usuário 1 — Pipeline CI/CD ativado por push (Prioridade: P1)

O desenvolvedor faz push de código na branch `main`. Automaticamente, o sistema
executa os testes, constrói a imagem Docker e publica no registro de imagens.
Se todos os passos passarem, o Railway detecta a nova imagem e realiza o
redeploy sem intervenção manual.

**Por que esta prioridade**: É o objetivo central da funcionalidade. Sem este
fluxo automatizado, todo o resto não tem valor.

**Teste Independente**: Fazer um push na branch `main` e verificar se o
workflow no GitHub Actions executa os três jobs (test → build-and-push → deploy)
com sucesso, e se a aplicação fica disponível na URL do Railway logo após.

**Cenários de Aceite**:

1. **Dado** que existe código na branch `main`, **Quando** o desenvolvedor faz
   push, **Então** o workflow `deploy-backend.yml` é disparado automaticamente.
2. **Dado** que o workflow foi disparado, **Quando** o job `test` executa,
   **Então** todos os testes passam e o job termina com sucesso.
3. **Dado** que `test` passou, **Quando** o job `build-and-push` executa,
   **Então** uma imagem Docker é publicada em `ghcr.io/<owner>/<repo>-backend:latest`.
4. **Dado** que a imagem foi publicada, **Quando** o job `deploy` executa,
   **Então** o Railway detecta a nova imagem e realiza o redeploy.
5. **Dado** que o redeploy foi concluído, **Quando** o desenvolvedor acessa
   `GET /api/boards`, **Então** recebe resposta HTTP 200.

---

### História de Usuário 2 — Proteção contra regressões (Prioridade: P2)

O desenvolvedor introduz uma alteração que quebra um teste existente e faz
push na `main`. O pipeline deve bloquear o deploy antes que código quebrado
chegue à produção.

**Por que esta prioridade**: Garante que o pipeline tem valor real como
salvaguarda — não apenas automatiza o deploy, mas impede deploys ruins.

**Teste Independente**: Introduzir um teste que falha propositalmente, fazer
push e verificar que os jobs `build-and-push` e `deploy` não são executados.

**Cenários de Aceite**:

1. **Dado** que um ou mais testes falham no job `test`, **Quando** o workflow
   executa, **Então** o job `test` termina com falha e os jobs seguintes não
   são executados.
2. **Dado** que o pipeline falhou, **Quando** o desenvolvedor corrige o código
   e faz novo push, **Então** o pipeline completa com sucesso do início ao fim.

---

### História de Usuário 3 — Aplicação rodando com PostgreSQL em produção (Prioridade: P3)

Após o deploy, a aplicação usa o banco PostgreSQL provisionado pelo Railway,
não o H2 in-memory. Os dados persistem entre reinicializações do serviço.

**Por que esta prioridade**: É o resultado final esperado do deploy — a
aplicação de produção deve ter persistência real.

**Teste Independente**: Criar um board via `POST /api/boards` em produção,
reiniciar o serviço no Railway e verificar que o board ainda existe via
`GET /api/boards`.

**Cenários de Aceite**:

1. **Dado** que a aplicação está em produção, **Quando** o desenvolvedor acessa
   `GET /actuator/health`, **Então** recebe `{"status":"UP"}` com HTTP 200.
2. **Dado** que o Railway injeta `DATABASE_URL` e `SPRING_PROFILES_ACTIVE=prod`,
   **Quando** a aplicação inicializa, **Então** conecta ao PostgreSQL e cria/atualiza
   as tabelas necessárias automaticamente.
3. **Dado** que dados foram criados em produção, **Quando** o serviço é
   reiniciado, **Então** os dados continuam disponíveis.

---

### Casos de Borda

- O que acontece se a variável `DATABASE_URL` não estiver configurada no Railway?
  A aplicação deve falhar na inicialização com mensagem clara de erro.
- O que acontece se o `RAILWAY_TOKEN` ou `GHCR_TOKEN` estiverem expirados?
  O job correspondente falha com mensagem de erro de autenticação, sem propagar
  código com segredos comprometidos.
- O que acontece se o push for em uma branch que não seja `main`?
  O workflow não é disparado (filtro por branch e path).
- O que acontece se o push em `main` alterar apenas arquivos fora de `backend/**`?
  O workflow não é disparado (filtro por path).

## Requisitos *(obrigatório)*

### Requisitos Funcionais

- **RF-001**: O sistema DEVE disparar o pipeline automaticamente a cada push na
  branch `main` com alterações em `backend/**`.
- **RF-002**: O pipeline DEVE executar todos os testes antes de qualquer build
  ou deploy.
- **RF-003**: O pipeline DEVE interromper a execução imediatamente se qualquer
  teste falhar, impedindo build e deploy.
- **RF-004**: O pipeline DEVE construir uma imagem Docker usando build multistage
  (stage de build separado do runtime) para minimizar o tamanho da imagem final.
- **RF-005**: O pipeline DEVE publicar a imagem Docker no GitHub Container Registry
  com a tag `latest` após os testes passarem.
- **RF-006**: O pipeline DEVE acionar o redeploy no Railway após a publicação
  bem-sucedida da imagem.
- **RF-007**: A aplicação em produção DEVE ler as configurações de banco de dados
  exclusivamente de variáveis de ambiente, sem credenciais no código-fonte ou
  nos arquivos versionados.
- **RF-008**: A aplicação em produção DEVE usar PostgreSQL como banco de dados,
  com esquema atualizado automaticamente a cada deploy.
- **RF-009**: A aplicação em produção DEVE responder na porta injetada pelo Railway
  via variável de ambiente `PORT`.
- **RF-010**: A aplicação em produção DEVE expor endpoint de healthcheck em
  `/actuator/health` retornando `{"status":"UP"}` quando saudável.
- **RF-011**: O ambiente de desenvolvimento local DEVE continuar funcionando com
  H2 in-memory sem nenhuma alteração no fluxo atual.
- **RF-012**: O cache de dependências do Maven DEVE ser reutilizado entre execuções
  do pipeline para reduzir o tempo de build.

### Entidades-Chave

- **Workflow CI/CD**: Conjunto de jobs (test, build-and-push, deploy) executados
  sequencialmente com dependências explícitas.
- **Imagem Docker**: Artefato construído a partir do código-fonte; publicado no
  registro e consumido pelo Railway.
- **Perfil de aplicação**: Configuração separada por ambiente (dev com H2, prod
  com PostgreSQL) selecionada via variável de ambiente.
- **Segredo**: Credencial sensível (token Railway, token GHCR, URL do banco)
  armazenada exclusivamente em variáveis de ambiente da plataforma, nunca no código.

## Critérios de Sucesso *(obrigatório)*

### Resultados Mensuráveis

- **CS-001**: Push na branch `main` dispara o workflow em menos de 30 segundos.
- **CS-002**: Pipeline completo (test + build + deploy) conclui em menos de 10 minutos
  em condições normais.
- **CS-003**: Falha em qualquer teste impede 100% dos deploys para produção.
- **CS-004**: Nenhuma credencial (token, senha, URL de banco) aparece em texto
  claro em qualquer arquivo versionado no repositório.
- **CS-005**: `GET /actuator/health` retorna `{"status":"UP"}` em menos de 2 segundos
  após o redeploy ser concluído.
- **CS-006**: `GET /api/boards` retorna HTTP 200 em produção após o primeiro deploy.
- **CS-007**: O ambiente de desenvolvimento local (`./mvnw spring-boot:run`) continua
  funcionando sem nenhuma configuração adicional.

## Premissas

- O repositório já existe no GitHub com o código do backend em Java 21 + Spring Boot 3.3.
- O projeto já possui um `Dockerfile` funcional ou será criado nesta funcionalidade
  conforme especificado.
- A conta do Railway já existe e o projeto foi criado no dashboard.
- O plugin de PostgreSQL foi adicionado ao projeto no Railway dashboard.
- O Railway está configurado para monitorar a imagem no `ghcr.io` e fazer redeploy
  automático ao detectar nova versão.
- O desenvolvedor configura manualmente os segredos no GitHub (`RAILWAY_TOKEN`,
  `GHCR_TOKEN`) e as variáveis de ambiente no Railway (`DATABASE_URL`,
  `SPRING_PROFILES_ACTIVE`) — isso está fora do escopo automatizado desta spec.
- O tier gratuito do Railway é suficiente para o volume de uso esperado (uso pessoal).
- Não há requisito de múltiplos ambientes (sem staging); apenas desenvolvimento
  local e produção.
