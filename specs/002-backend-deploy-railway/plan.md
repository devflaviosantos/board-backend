# Plano de Implementação: Deploy Contínuo do Backend no Railway

**Branch**: `002-backend-deploy-railway` | **Data**: 2026-04-21 | **Spec**: [spec.md](spec.md)
**Input**: Especificação de funcionalidade em `specs/002-backend-deploy-railway/spec.md`

## Resumo

Pipeline CI/CD completo para o backend Java 21 + Spring Boot 3.3 hospedado no
Railway (tier gratuito). Push na branch `main` dispara automaticamente: execução
dos testes → build da imagem Docker multistage → push para GHCR → redeploy no
Railway com PostgreSQL em produção. Desenvolvimento local continua usando H2
sem alterações no fluxo atual.

## Contexto Técnico

**Linguagem/Versão**: Java 21
**Dependências Principais**: Spring Boot 3.3, Spring Data JPA, Spring Validation,
Spring Boot Actuator, H2 (dev), PostgreSQL driver (prod)
**Armazenamento**: H2 in-memory (dev) / PostgreSQL provisionado pelo Railway (prod)
**Testes**: JUnit 5 + Mockito + @SpringBootTest + TestRestTemplate (existentes)
**Plataforma Alvo**: Railway (Linux container, tier gratuito)
**Tipo de Projeto**: web-service (API REST) + pipeline CI/CD
**Metas de Performance**: Pipeline completo em menos de 10 minutos
**Restrições**: Tier gratuito Railway; sem staging; sem domínio customizado;
segredos nunca no código; desenvolvimento local não pode quebrar
**Escopo**: Single user, MVP

## Verificação de Constituição

**Gates**:
- ✅ **Princípio I (Arquitetura MVC)**: Não alterado — nenhuma camada de código
  de negócio é modificada. Apenas configuração e infraestrutura.
- ✅ **Princípio II (Cobertura de Testes)**: O pipeline garante que todos os
  testes existentes passam antes de qualquer deploy.
- ✅ **Princípio III (Integridade em Cascata)**: Não alterado.
- ✅ **Princípio IV (Posicionamento Frontend)**: Não alterado.
- ✅ **Princípio V (Simplicidade / MVP)**: O deploy está alinhado ao escopo MVP
  (single user, tier gratuito, sem staging, sem autenticação).
- ✅ **Restrição Técnica (CORS)**: Não alterada. Em produção, será necessário
  ajustar `CorsConfig` para permitir a URL do Railway além de `localhost:4200`
  — documentado como item fora do escopo desta spec.
- ✅ **Nenhuma credencial no código**: `DATABASE_URL`, tokens e senhas são
  injetados exclusivamente via variáveis de ambiente de plataforma.

**Sem violações de constituição identificadas.**

## Estrutura do Projeto

### Documentação (esta funcionalidade)

```
specs/002-backend-deploy-railway/
├── plan.md          ← este arquivo
├── spec.md          ← especificação da funcionalidade
├── research.md      ← decisões técnicas (Fase 0)
├── data-model.md    ← estrutura dos artefatos de configuração (Fase 1)
├── quickstart.md    ← setup, pré-requisitos, execução e verificação (Fase 1)
├── contracts/
│   └── ci-cd.md     ← contrato do pipeline CI/CD (Fase 1)
└── tasks.md         ← gerado por /speckit-tasks (ainda não criado)
```

### Arquivos a criar ou modificar (raiz do repositório)

```
(raiz do repositório)
├── Dockerfile                                    ← CRIAR: multistage build
├── railway.json                                  ← CRIAR: config Railway
├── pom.xml                                       ← MODIFICAR: +actuator, +postgresql
├── .github/
│   └── workflows/
│       └── deploy-backend.yml                    ← CRIAR: pipeline CI/CD
└── src/
    └── main/
        └── resources/
            ├── application.yml                   ← MODIFICAR: ajustar perfil dev
            └── application-prod.yml              ← CRIAR: config PostgreSQL prod
```

**Decisão de Estrutura**: Projeto único Maven na raiz. Não há subdiretório
`backend/` — o repositório `board-backend` contém exclusivamente o backend.
O Dockerfile e `railway.json` ficam na raiz para compatibilidade com o Railway.

## Principais Decisões de Design

| Decisão | Escolha | Ver |
|---------|---------|-----|
| Localização do Dockerfile | Raiz do repositório | [research.md](research.md#decisão-1) |
| Base runtime da imagem | `eclipse-temurin:21-jre-alpine` | [research.md](research.md#decisão-2) |
| Separação dev/prod | `application.yml` (H2) + `application-prod.yml` (PostgreSQL) | [research.md](research.md#decisão-3) |
| Leitura do DATABASE_URL | `jdbc:${DATABASE_URL}` no Spring | [research.md](research.md#decisão-4) |
| Dependências novas | Actuator + PostgreSQL driver | [research.md](research.md#decisão-5) |
| Path filter do workflow | `src/**`, `pom.xml`, `Dockerfile`, `railway.json` | [research.md](research.md#decisão-6) |
| Mecanismo de deploy | `railway-app/railway-action@v1` | [research.md](research.md#decisão-7) |
| Porta do servidor | `${PORT:8080}` | [research.md](research.md#decisão-8) |

## Artefatos Gerados

| Artefato | Fase | Descrição |
|----------|------|-----------|
| [research.md](research.md) | 0 | Decisões técnicas e alternativas consideradas |
| [data-model.md](data-model.md) | 1 | Estrutura de todos os artefatos de configuração |
| [contracts/ci-cd.md](contracts/ci-cd.md) | 1 | Contrato do pipeline CI/CD com jobs, triggers e segredos |
| [quickstart.md](quickstart.md) | 1 | Pré-requisitos, configuração e verificação do deploy |

## Próximo Passo

```
/speckit-tasks
```
