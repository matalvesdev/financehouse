# Resumo: Funcionalidades Pendentes

## 📋 Visão Geral

Este documento resume as funcionalidades que precisam ser implementadas para completar o Sistema de Gestão Financeira Doméstica.

## 🎯 Status Atual

### ✅ Implementado e Funcionando

- **Autenticação**: Login, registro, JWT tokens, refresh, logout
- **Transações**: CRUD completo (criar, listar, atualizar, excluir, reativar)
- **Orçamentos**: Criar e listar (CRUD parcial)
- **Metas**: Criar e listar (CRUD parcial)
- **Frontend**: Todas as páginas criadas e funcionais
- **Infraestrutura**: Docker, PostgreSQL, Flyway migrations
- **Segurança**: JWT, password hashing, encryption
- **Testes**: Property-based tests, unit tests, integration tests

### ❌ Faltando Implementar

#### Priority 1 (Crítico - Bloqueia UX)

1. **DashboardController** - Endpoint `/dashboard/resumo`
   - Status: ❌ Não implementado
   - Impacto: Dashboard page mostra erro 500
   - Esforço: 1-1.5 dias

2. **ImportController** - Endpoint `/importacao/upload`
   - Status: ❌ Não implementado
   - Impacto: Import page mostra erro 500
   - Esforço: 2-2.5 dias

#### Priority 2 (Alto - Completa Features Core)

3. **Budget CRUD Completo**
   - Status: ⚠️ Parcialmente implementado
   - Faltando:
     - `GET /orcamentos/{id}` - Obter por ID
     - `PUT /orcamentos/{id}` - Atualizar
     - `DELETE /orcamentos/{id}` - Excluir
   - Esforço: 1-1.5 dias

4. **Goal CRUD Completo**
   - Status: ⚠️ Parcialmente implementado
   - Faltando:
     - `GET /metas/{id}` - Obter por ID
     - `PUT /metas/{id}` - Atualizar
     - `PATCH /metas/{id}/progresso` - Atualizar progresso
     - `DELETE /metas/{id}` - Excluir
   - Esforço: 1-1.5 dias

## 📊 Detalhamento das Funcionalidades

### 1. Dashboard Summary Endpoint

**Endpoint**: `GET /api/dashboard/resumo`

**Funcionalidade**: Retorna resumo consolidado da situação financeira do usuário

**Response**:
```json
{
  "saldoAtual": 5000.00,
  "receitaMensal": 3000.00,
  "despesaMensal": 1500.00,
  "statusOrcamentos": [
    {
      "id": "uuid",
      "categoria": "ALIMENTACAO",
      "limite": 500.00,
      "gastoAtual": 350.00,
      "percentualGasto": 70.00,
      "status": "ATIVO"
    }
  ],
  "progressoMetas": [
    {
      "id": "uuid",
      "nome": "Viagem",
      "valorAlvo": 10000.00,
      "valorAtual": 3000.00,
      "percentualConclusao": 30.00,
      "prazo": "2026-12-31",
      "dataEstimadaConclusao": "2026-11-15",
      "status": "ATIVA"
    }
  ],
  "transacoesRecentes": [...],
  "resumoInvestimentos": null
}
```

**Componentes Necessários**:
- `ObterResumoDashboardUseCase` (Application Layer)
- `DashboardController` (Web Layer)
- Testes unitários e de integração

### 2. Spreadsheet Import Endpoint

**Endpoint**: `POST /api/importacao/upload`

**Funcionalidade**: Processa upload de planilha Excel/CSV e importa transações

**Request**: `multipart/form-data` com arquivo

**Response**:
```json
{
  "totalLinhas": 100,
  "importadasComSucesso": 95,
  "falhas": 5,
  "duplicatasPotenciais": [
    {
      "linha": 10,
      "descricao": "Compra supermercado",
      "valor": 150.00,
      "data": "2026-01-15",
      "motivoSimilaridade": "Transação similar encontrada"
    }
  ],
  "erros": [
    {
      "linha": 20,
      "descricao": "Linha inválida",
      "mensagem": "Data é obrigatória"
    }
  ],
  "transacoesSalvas": [...]
}
```

**Componentes Necessários**:
- `ImportarPlanilhaUseCase` (Application Layer)
- `ImportController` (Web Layer)
- DTOs: `ComandoImportarPlanilha`, `ResultadoImportacaoResponse`
- Configuração de upload no Spring Boot
- Testes unitários e de integração

### 3. Budget CRUD Completo

**Endpoints Faltantes**:

```
GET    /api/orcamentos/{id}      - Obter orçamento por ID
PUT    /api/orcamentos/{id}      - Atualizar orçamento
DELETE /api/orcamentos/{id}      - Excluir orçamento (soft delete)
```

**Componentes Necessários**:
- `ObterOrcamentoPorIdUseCase`
- `AtualizarOrcamentoUseCase`
- `ExcluirOrcamentoUseCase`
- DTOs: `AtualizarOrcamentoRequest`, `ComandoAtualizarOrcamento`
- Endpoints no `OrcamentoController`
- Testes unitários e de integração

### 4. Goal CRUD Completo

**Endpoints Faltantes**:

```
GET    /api/metas/{id}           - Obter meta por ID
PUT    /api/metas/{id}           - Atualizar meta
PATCH  /api/metas/{id}/progresso - Atualizar progresso
DELETE /api/metas/{id}           - Excluir meta (soft delete)
```

**Componentes Necessários**:
- `ObterMetaPorIdUseCase`
- `AtualizarMetaUseCase`
- `AtualizarProgressoMetaUseCase`
- `ExcluirMetaUseCase`
- DTOs: `AtualizarMetaRequest`, `AtualizarProgressoRequest`, comandos
- Endpoints no `MetaFinanceiraController`
- Testes unitários e de integração

## 🏗️ Arquitetura

Todas as implementações seguem a arquitetura hexagonal existente:

```
┌─────────────────────────────────────────────────────────────┐
│                        Web Layer                             │
│  Controllers: DashboardController, ImportController          │
│  (REST endpoints, autenticação JWT, validação)              │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
│  Use Cases: ObterResumoDashboardUseCase,                    │
│             ImportarPlanilhaUseCase, etc.                    │
│  (Lógica de orquestração, validação de negócio)            │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Domain Layer                            │
│  Entities: Usuario, Transacao, Orcamento, MetaFinanceira   │
│  Value Objects: Email, Valor, Categoria, etc.              │
│  (Regras de negócio puras, sem dependências)               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                        │
│  Repositories: JPA implementations                           │
│  Adapters: ProcessadorPlanilhaAdapter                       │
│  (Persistência, integrações externas)                       │
└─────────────────────────────────────────────────────────────┘
```

## 📝 Checklist de Implementação

### Priority 1 (Crítico)

- [ ] **Dashboard**
  - [ ] Criar `ObterResumoDashboardUseCase`
  - [ ] Criar `DashboardController`
  - [ ] Escrever testes unitários
  - [ ] Escrever testes de integração
  - [ ] Testar no frontend

- [ ] **Import**
  - [ ] Criar `ImportarPlanilhaUseCase`
  - [ ] Criar `ImportController`
  - [ ] Criar DTOs necessários
  - [ ] Configurar upload no Spring Boot
  - [ ] Escrever testes unitários
  - [ ] Escrever testes de integração
  - [ ] Testar no frontend

### Priority 2 (Alto)

- [ ] **Budget CRUD**
  - [ ] Criar `ObterOrcamentoPorIdUseCase`
  - [ ] Criar `AtualizarOrcamentoUseCase`
  - [ ] Criar `ExcluirOrcamentoUseCase`
  - [ ] Adicionar endpoints no controller
  - [ ] Escrever testes

- [ ] **Goal CRUD**
  - [ ] Criar `ObterMetaPorIdUseCase`
  - [ ] Criar `AtualizarMetaUseCase`
  - [ ] Criar `AtualizarProgressoMetaUseCase`
  - [ ] Criar `ExcluirMetaUseCase`
  - [ ] Adicionar endpoints no controller
  - [ ] Escrever testes

### Priority 3 (Médio)

- [ ] **Validação End-to-End**
  - [ ] Testar fluxo completo de dashboard
  - [ ] Testar fluxo completo de importação
  - [ ] Testar fluxo completo de CRUD de orçamentos
  - [ ] Testar fluxo completo de CRUD de metas

- [ ] **Performance**
  - [ ] Testar dashboard com muitos dados
  - [ ] Testar importação de arquivos grandes

- [ ] **Documentação**
  - [ ] Atualizar documentação da API
  - [ ] Atualizar README.md
  - [ ] Atualizar TESTING_STATUS.md

## ⏱️ Estimativa de Esforço

| Prioridade | Funcionalidade | Esforço Estimado |
|------------|----------------|------------------|
| P1 | Dashboard Controller | 1-1.5 dias |
| P1 | Import Controller | 2-2.5 dias |
| P2 | Budget CRUD Completo | 1-1.5 dias |
| P2 | Goal CRUD Completo | 1-1.5 dias |
| P3 | Validação e Testes | 1-2 dias |
| **TOTAL** | | **6-9 dias** |

## 🎯 Critérios de Sucesso

A implementação será considerada completa quando:

1. ✅ Dashboard page carrega sem erros (sem 500)
2. ✅ Import page funciona completamente
3. ✅ Todos os endpoints CRUD funcionam
4. ✅ Todos os testes passam (>80% cobertura)
5. ✅ Testes manuais confirmam funcionalidade
6. ✅ Documentação está atualizada
7. ✅ Aplicação pronta para produção

## 📚 Documentos Relacionados

- **Requirements**: `.kiro/specs/funcionalidades-pendentes/requirements.md`
  - Especificação detalhada de todos os requisitos
  - Acceptance criteria para cada funcionalidade
  - Priorização e classificação

- **Design**: `.kiro/specs/funcionalidades-pendentes/design.md`
  - Design técnico detalhado
  - Arquitetura de componentes
  - Exemplos de código
  - Estratégia de testes

- **Tasks**: `.kiro/specs/funcionalidades-pendentes/tasks.md`
  - Plano de implementação detalhado
  - Tarefas organizadas por prioridade
  - Subtarefas específicas
  - Estimativas de esforço

- **UI/UX Guidelines**: `.kiro/specs/funcionalidades-pendentes/UI_UX_GUIDELINES.md`
  - Diretrizes de interface e experiência do usuário
  - Uso de shadcn/ui, DaisyUI e TailwindCSS
  - Componentes por funcionalidade
  - Padrões de responsividade e acessibilidade

- **Design System**: `.kiro/specs/funcionalidades-pendentes/DESIGN_SYSTEM.md`
  - Design system completo minimalista e moderno
  - Tipografia com DM Sans
  - Paleta de cores (verde, preto, branco)
  - Cores de status (verde, laranja, vermelho)
  - Sistema de espaçamento e componentes
  - Configuração TailwindCSS e DaisyUI
  - Exemplos práticos de implementação

## 🚀 Próximos Passos

1. **Revisar specs**: Ler requirements.md, design.md e tasks.md
2. **Começar por Priority 1**: Dashboard e Import são críticos
3. **Seguir arquitetura existente**: Manter padrões do projeto
4. **Testar continuamente**: Escrever testes junto com código
5. **Validar no frontend**: Testar integração após cada endpoint
6. **Documentar**: Atualizar docs conforme implementa

## 📞 Suporte

Para dúvidas sobre a implementação:
- Consultar design.md para detalhes técnicos
- Consultar código existente para padrões
- Seguir mesma estrutura de controllers/use cases existentes
- Manter consistência com DTOs e responses atuais

---

**Status**: Specs completas e prontas para implementação
**Data**: 2026-02-01
**Versão**: 1.0
