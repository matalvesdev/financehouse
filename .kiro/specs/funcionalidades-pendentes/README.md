# Spec: Funcionalidades Pendentes

## 📖 Sobre Esta Spec

Esta spec documenta todas as funcionalidades que ainda precisam ser implementadas no Sistema de Gestão Financeira Doméstica para completar a aplicação conforme o design original.

## 📁 Estrutura da Spec

```
.kiro/specs/funcionalidades-pendentes/
├── README.md                 # Este arquivo - visão geral da spec
├── RESUMO.md                # Resumo executivo das funcionalidades
├── requirements.md          # Requisitos detalhados com acceptance criteria
├── design.md               # Design técnico e arquitetura
├── tasks.md                # Plano de implementação detalhado
├── EXEMPLOS.md             # Templates e exemplos de código
├── UI_UX_GUIDELINES.md     # Diretrizes de UI/UX (shadcn/ui, DaisyUI, TailwindCSS)
└── DESIGN_SYSTEM.md        # 🎨 Design System completo (cores, tipografia, componentes)
```

## 🎯 Objetivo

Completar a implementação do Sistema de Gestão Financeira Doméstica, focando em:

1. **Dashboard Controller** - Endpoint para resumo financeiro consolidado
2. **Import Controller** - Endpoint para upload e processamento de planilhas
3. **Budget CRUD Completo** - Operações faltantes (GET by ID, UPDATE, DELETE)
4. **Goal CRUD Completo** - Operações faltantes (GET by ID, UPDATE, UPDATE PROGRESS, DELETE)

## 📊 Status Atual

### ✅ O Que Já Funciona

- Autenticação completa (login, registro, JWT, refresh, logout)
- Transações CRUD completo
- Orçamentos CREATE e READ
- Metas CREATE e READ
- Frontend completo (todas as páginas criadas)
- Infraestrutura (Docker, PostgreSQL, Flyway)
- Segurança (JWT, password hashing, encryption)
- Testes (property-based, unit, integration)

### ❌ O Que Está Faltando

#### Priority 1 (Crítico)
- ❌ DashboardController - `/dashboard/resumo` (500 error)
- ❌ ImportController - `/importacao/upload` (500 error)

#### Priority 2 (Alto)
- ⚠️ Budget CRUD - Faltam GET by ID, UPDATE, DELETE
- ⚠️ Goal CRUD - Faltam GET by ID, UPDATE, UPDATE PROGRESS, DELETE

## 📚 Como Usar Esta Spec

### 1. Comece pelo RESUMO.md

Leia o [RESUMO.md](./RESUMO.md) para entender rapidamente:
- O que precisa ser implementado
- Prioridades
- Estimativas de esforço
- Critérios de sucesso

### 2. Leia os Requirements

Consulte [requirements.md](./requirements.md) para:
- Requisitos detalhados de cada funcionalidade
- Acceptance criteria específicos
- Classificação de prioridades
- Constraints técnicos

### 3. Estude o Design

Revise [design.md](./design.md) para:
- Arquitetura de componentes
- Exemplos de código detalhados
- Estratégia de testes
- Considerações de segurança e performance

### 4. Siga o Plano de Tasks

Use [tasks.md](./tasks.md) para:
- Plano de implementação passo a passo
- Tarefas organizadas por prioridade
- Subtarefas específicas
- Estimativas de esforço

### 5. Use os Exemplos

Consulte [EXEMPLOS.md](./EXEMPLOS.md) para:
- Templates de código prontos
- Exemplos completos de implementação
- Checklists de implementação
- Padrões a seguir

### 6. Siga as Diretrizes de UI/UX

Consulte [UI_UX_GUIDELINES.md](./UI_UX_GUIDELINES.md) para:
- Como usar shadcn/ui, DaisyUI e TailwindCSS
- Design system e padrões visuais
- Componentes específicos por funcionalidade
- Responsividade e acessibilidade

## 🚀 Começando a Implementação

### Passo 1: Escolha uma Prioridade

Recomendamos começar por **Priority 1** (Dashboard e Import), pois são críticos para a experiência do usuário.

### Passo 2: Leia a Documentação Relevante

Para cada funcionalidade:
1. Leia os requirements específicos
2. Estude o design técnico
3. Revise os exemplos de código
4. Entenda os testes necessários

### Passo 3: Implemente Seguindo o Padrão

Siga a arquitetura hexagonal existente:
```
Controller → Use Case → Domain Entity → Repository
```

### Passo 4: Escreva Testes

Para cada implementação:
- Testes unitários (>80% cobertura)
- Testes de integração
- Validação manual

### Passo 5: Valide no Frontend

Após implementar cada endpoint:
- Teste via Postman/curl
- Teste via frontend
- Verifique que não há erros 500

## 📋 Checklist Rápido

### Dashboard Controller
- [ ] Criar `ObterResumoDashboardUseCase`
- [ ] Criar `DashboardController`
- [ ] Escrever testes unitários
- [ ] Escrever testes de integração
- [ ] Testar no frontend

### Import Controller
- [ ] Criar `ImportarPlanilhaUseCase`
- [ ] Criar `ImportController`
- [ ] Criar DTOs necessários
- [ ] Configurar upload no Spring Boot
- [ ] Escrever testes
- [ ] Testar no frontend

### Budget CRUD
- [ ] Criar `ObterOrcamentoPorIdUseCase`
- [ ] Criar `AtualizarOrcamentoUseCase`
- [ ] Criar `ExcluirOrcamentoUseCase`
- [ ] Adicionar endpoints no controller
- [ ] Escrever testes

### Goal CRUD
- [ ] Criar `ObterMetaPorIdUseCase`
- [ ] Criar `AtualizarMetaUseCase`
- [ ] Criar `AtualizarProgressoMetaUseCase`
- [ ] Criar `ExcluirMetaUseCase`
- [ ] Adicionar endpoints no controller
- [ ] Escrever testes

## ⏱️ Estimativa de Tempo

| Funcionalidade | Esforço |
|----------------|---------|
| Dashboard Controller | 1-1.5 dias |
| Import Controller | 2-2.5 dias |
| Budget CRUD Completo | 1-1.5 dias |
| Goal CRUD Completo | 1-1.5 dias |
| Validação e Testes | 1-2 dias |
| **TOTAL** | **6-9 dias** |

## 🎯 Critérios de Sucesso

A implementação será considerada completa quando:

1. ✅ Dashboard page carrega sem erros
2. ✅ Import page funciona completamente
3. ✅ Todos os endpoints CRUD funcionam
4. ✅ Todos os testes passam (>80% cobertura)
5. ✅ Testes manuais confirmam funcionalidade
6. ✅ Documentação está atualizada
7. ✅ Aplicação pronta para produção

## 📞 Suporte

### Dúvidas sobre Requisitos?
Consulte [requirements.md](./requirements.md)

### Dúvidas sobre Design?
Consulte [design.md](./design.md)

### Dúvidas sobre Implementação?
Consulte [EXEMPLOS.md](./EXEMPLOS.md)

### Dúvidas sobre Tarefas?
Consulte [tasks.md](./tasks.md)

## 🔗 Links Relacionados

- **Spec Principal**: `.kiro/specs/gestao-financeira-domestica/`
- **Testing Status**: `TESTING_STATUS.md`
- **Local Testing Guide**: `LOCAL_TESTING_GUIDE.md`
- **Authentication Fix**: `AUTHENTICATION_FIX_SUMMARY.md`

## 📝 Notas Importantes

1. **Siga a Arquitetura Existente**: Todas as implementações devem seguir a arquitetura hexagonal já estabelecida
2. **Mantenha Consistência**: Use os mesmos padrões de código, nomenclatura e estrutura
3. **Teste Continuamente**: Escreva testes junto com o código, não depois
4. **Valide no Frontend**: Teste cada endpoint no frontend após implementar
5. **Documente**: Mantenha JavaDoc e comentários atualizados

## 🎓 Aprendizados do Projeto

### Arquitetura Hexagonal
- Separação clara de responsabilidades
- Domain layer puro (sem dependências externas)
- Use cases orquestram operações de negócio
- Ports definem interfaces para infraestrutura

### Segurança
- JWT para autenticação stateless
- Password hashing com SHA-256 + salt
- Encryption at rest para dados sensíveis
- Validação de propriedade de recursos

### Testes
- Property-based tests para propriedades universais
- Unit tests para casos específicos
- Integration tests para endpoints
- >80% de cobertura de código

### Frontend
- React + TypeScript + Vite
- Zustand para state management
- Zod para validação de formulários
- Axios com interceptors para JWT

## 🚀 Próximos Passos

1. **Leia o RESUMO.md** para visão geral
2. **Escolha uma funcionalidade Priority 1** para começar
3. **Leia requirements e design** da funcionalidade escolhida
4. **Use os templates do EXEMPLOS.md** para acelerar
5. **Siga o tasks.md** para não perder nada
6. **Teste continuamente** durante a implementação
7. **Valide no frontend** após cada endpoint

---

**Status**: Spec completa e pronta para implementação  
**Data**: 2026-02-01  
**Versão**: 1.0  
**Autor**: Sistema de Gestão Financeira Doméstica Team

---

## 📖 Leitura Recomendada

1. Comece por: [RESUMO.md](./RESUMO.md)
2. Depois leia: [requirements.md](./requirements.md)
3. Estude: [design.md](./design.md)
4. Revise UI/UX: [UI_UX_GUIDELINES.md](./UI_UX_GUIDELINES.md)
5. Use como guia: [tasks.md](./tasks.md)
6. Consulte quando precisar: [EXEMPLOS.md](./EXEMPLOS.md)

**Boa implementação! 🚀**
