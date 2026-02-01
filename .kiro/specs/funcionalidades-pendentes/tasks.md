# Implementation Plan: Funcionalidades Pendentes

## Overview

Este plano de implementação detalha as tarefas necessárias para completar as funcionalidades pendentes do Sistema de Gestão Financeira Doméstica. O foco está em implementar os controllers faltantes (Dashboard e Import) e completar as operações CRUD para orçamentos e metas.

## Priority Order

As tarefas estão organizadas por prioridade:
- **Priority 1 (P1)**: Crítico - Bloqueia experiência do usuário
- **Priority 2 (P2)**: Alto - Completa funcionalidades core
- **Priority 3 (P3)**: Médio - Melhorias e refinamentos

## Tasks

### Priority 1: Dashboard Controller (Critical)

- [ ] 1. Implementar Dashboard Use Case
  - [ ] 1.1 Criar ObterResumoDashboardUseCase
    - Implementar cálculo de saldo atual (soma de todas as transações)
    - Implementar cálculo de receita mensal (mês corrente)
    - Implementar cálculo de despesa mensal (mês corrente)
    - Implementar obtenção de status de orçamentos ativos
    - Implementar obtenção de progresso de metas ativas
    - Implementar obtenção de transações recentes (últimas 10)
    - Implementar obtenção de resumo de investimentos (retornar null por enquanto)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8_
  
  - [ ] 1.2 Escrever testes unitários para ObterResumoDashboardUseCase
    - Testar cálculo de saldo com múltiplas transações
    - Testar cálculo de receita mensal
    - Testar cálculo de despesa mensal
    - Testar retorno de resumo vazio quando usuário não tem dados
    - Testar agregação correta de orçamentos e metas
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

- [ ] 2. Implementar Dashboard Controller
  - [ ] 2.1 Criar DashboardController
    - Criar endpoint GET /dashboard/resumo
    - Implementar autenticação JWT
    - Implementar validação de usuário
    - Implementar tratamento de erros
    - _Requirements: 1.1, 1.10, 1.11_
  
  - [ ] 2.2 Escrever testes de integração para DashboardController
    - Testar endpoint com usuário autenticado
    - Testar endpoint sem autenticação (deve retornar 401)
    - Testar endpoint com dados completos
    - Testar endpoint com usuário sem dados
    - Validar estrutura do JSON de resposta
    - _Requirements: 1.1, 1.10, 1.11_

- [ ] 3. Validar Dashboard no Frontend
  - [ ] 3.1 Testar integração frontend-backend
    - Verificar que dashboard page carrega sem erros
    - Verificar que dados são exibidos corretamente
    - Verificar que gráficos são renderizados
    - Verificar que cards de orçamento e metas aparecem
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_

### Priority 1: Import Controller (Critical)

- [ ] 4. Implementar Import Use Case
  - [ ] 4.1 Criar ImportarPlanilhaUseCase
    - Implementar validação de usuário
    - Implementar processamento de planilha via ProcessadorPlanilhaPort
    - Implementar extração de transações
    - Implementar detecção de duplicatas
    - Implementar validação de transações importadas
    - Implementar salvamento de transações válidas
    - Implementar coleta de erros de validação
    - Implementar marcação de dados iniciais carregados
    - Implementar registro de auditoria
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10, 2.11_
  
  - [ ] 4.2 Criar DTOs para importação
    - Criar ComandoImportarPlanilha
    - Criar ArquivoPlanilha
    - Criar TransacaoImportada
    - Criar DadosPlanilha
    - Criar DuplicataPotencial
    - _Requirements: 2.1, 2.3, 2.6_
  
  - [ ] 4.3 Escrever testes unitários para ImportarPlanilhaUseCase
    - Testar importação bem-sucedida com dados válidos
    - Testar detecção de duplicatas
    - Testar validação de campos obrigatórios
    - Testar tratamento de erros de validação
    - Testar marcação de dados iniciais carregados
    - Testar que transações inválidas não são salvas
    - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9, 2.10_

- [ ] 5. Implementar Import Controller
  - [ ] 5.1 Criar ImportController
    - Criar endpoint POST /importacao/upload
    - Implementar recebimento de MultipartFile
    - Implementar validação de arquivo (tamanho, formato)
    - Implementar autenticação JWT
    - Implementar tratamento de erros
    - _Requirements: 2.1, 2.2, 2.12, 2.13, 2.14_
  
  - [ ] 5.2 Configurar upload de arquivos no Spring Boot
    - Configurar spring.servlet.multipart.max-file-size=10MB
    - Configurar spring.servlet.multipart.max-request-size=10MB
    - Configurar spring.servlet.multipart.enabled=true
    - _Requirements: 2.14_
  
  - [ ] 5.3 Escrever testes de integração para ImportController
    - Testar upload de arquivo Excel válido
    - Testar upload de arquivo CSV válido
    - Testar rejeição de arquivo muito grande (>10MB)
    - Testar rejeição de formato inválido
    - Testar upload sem autenticação (deve retornar 401)
    - Validar estrutura do JSON de resposta
    - _Requirements: 2.1, 2.2, 2.12, 2.13, 2.14_

- [ ] 6. Validar Import no Frontend
  - [ ] 6.1 Testar integração frontend-backend
    - Verificar que import page carrega sem erros
    - Verificar que upload de arquivo funciona
    - Verificar que preview de dados aparece
    - Verificar que duplicatas são destacadas
    - Verificar que erros são exibidos corretamente
    - Verificar que resultado da importação é mostrado
    - _Requirements: 2.1, 2.2, 2.3, 2.5, 2.8, 2.11_

### Priority 2: Complete Budget CRUD (High)

- [ ] 7. Implementar Budget Use Cases Faltantes
  - [ ] 7.1 Criar ObterOrcamentoPorIdUseCase
    - Implementar busca de orçamento por ID
    - Implementar validação de propriedade (usuário dono)
    - Implementar tratamento de orçamento não encontrado
    - _Requirements: 4.5, 4.8, 4.10_
  
  - [ ] 7.2 Criar AtualizarOrcamentoUseCase
    - Implementar busca de orçamento existente
    - Implementar validação de propriedade
    - Implementar atualização de campos
    - Implementar preservação de audit trail
    - Implementar salvamento
    - _Requirements: 4.3, 4.6, 4.10_
  
  - [ ] 7.3 Criar ExcluirOrcamentoUseCase
    - Implementar busca de orçamento existente
    - Implementar validação de propriedade
    - Implementar soft delete (marcar como inativo)
    - Implementar salvamento
    - _Requirements: 4.4, 4.7, 4.10_
  
  - [ ] 7.4 Criar DTOs para operações de orçamento
    - Criar AtualizarOrcamentoRequest
    - Criar ComandoAtualizarOrcamento
    - _Requirements: 4.3_
  
  - [ ] 7.5 Escrever testes unitários para Budget Use Cases
    - Testar ObterOrcamentoPorIdUseCase com ID válido
    - Testar ObterOrcamentoPorIdUseCase com ID inválido
    - Testar ObterOrcamentoPorIdUseCase com orçamento de outro usuário
    - Testar AtualizarOrcamentoUseCase com dados válidos
    - Testar AtualizarOrcamentoUseCase com orçamento de outro usuário
    - Testar ExcluirOrcamentoUseCase com ID válido
    - Testar ExcluirOrcamentoUseCase com orçamento de outro usuário
    - _Requirements: 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.10_

- [ ] 8. Adicionar Endpoints no OrcamentoController
  - [ ] 8.1 Implementar GET /orcamentos/{id}
    - Adicionar endpoint no controller
    - Implementar autenticação JWT
    - Implementar validação de UUID
    - Implementar tratamento de erros
    - _Requirements: 4.5, 4.8, 4.9_
  
  - [ ] 8.2 Implementar PUT /orcamentos/{id}
    - Adicionar endpoint no controller
    - Implementar autenticação JWT
    - Implementar validação de request body
    - Implementar tratamento de erros
    - _Requirements: 4.3, 4.6, 4.9_
  
  - [ ] 8.3 Implementar DELETE /orcamentos/{id}
    - Adicionar endpoint no controller
    - Implementar autenticação JWT
    - Implementar validação de UUID
    - Implementar tratamento de erros
    - _Requirements: 4.4, 4.7, 4.9_
  
  - [ ] 8.4 Escrever testes de integração para novos endpoints
    - Testar GET /orcamentos/{id} com ID válido
    - Testar GET /orcamentos/{id} com ID inválido (404)
    - Testar GET /orcamentos/{id} sem autenticação (401)
    - Testar PUT /orcamentos/{id} com dados válidos
    - Testar PUT /orcamentos/{id} com dados inválidos (400)
    - Testar PUT /orcamentos/{id} sem autenticação (401)
    - Testar DELETE /orcamentos/{id} com ID válido
    - Testar DELETE /orcamentos/{id} sem autenticação (401)
    - _Requirements: 4.3, 4.4, 4.5, 4.9_

### Priority 2: Complete Goal CRUD (High)

- [ ] 9. Implementar Goal Use Cases Faltantes
  - [ ] 9.1 Criar ObterMetaPorIdUseCase
    - Implementar busca de meta por ID
    - Implementar validação de propriedade (usuário dono)
    - Implementar tratamento de meta não encontrada
    - _Requirements: 5.5, 5.12, 5.13_
  
  - [ ] 9.2 Criar AtualizarMetaUseCase
    - Implementar busca de meta existente
    - Implementar validação de propriedade
    - Implementar atualização de campos
    - Implementar preservação de audit trail
    - Implementar salvamento
    - _Requirements: 5.3, 5.7, 5.13_
  
  - [ ] 9.3 Criar AtualizarProgressoMetaUseCase
    - Implementar busca de meta existente
    - Implementar validação de propriedade
    - Implementar adição de progresso
    - Implementar recálculo de percentual de conclusão
    - Implementar recálculo de data estimada de conclusão
    - Implementar marcação automática como CONCLUIDA se atingir 100%
    - Implementar salvamento
    - _Requirements: 5.6, 5.8, 5.9, 5.10, 5.13_
  
  - [ ] 9.4 Criar ExcluirMetaUseCase
    - Implementar busca de meta existente
    - Implementar validação de propriedade
    - Implementar soft delete (marcar como inativa)
    - Implementar salvamento
    - _Requirements: 5.4, 5.11, 5.13_
  
  - [ ] 9.5 Criar DTOs para operações de meta
    - Criar AtualizarMetaRequest
    - Criar AtualizarProgressoRequest
    - Criar ComandoAtualizarMeta
    - Criar ComandoAtualizarProgressoMeta
    - _Requirements: 5.3, 5.6_
  
  - [ ] 9.6 Escrever testes unitários para Goal Use Cases
    - Testar ObterMetaPorIdUseCase com ID válido
    - Testar ObterMetaPorIdUseCase com ID inválido
    - Testar ObterMetaPorIdUseCase com meta de outro usuário
    - Testar AtualizarMetaUseCase com dados válidos
    - Testar AtualizarMetaUseCase com meta de outro usuário
    - Testar AtualizarProgressoMetaUseCase com valor válido
    - Testar AtualizarProgressoMetaUseCase marca como CONCLUIDA ao atingir 100%
    - Testar ExcluirMetaUseCase com ID válido
    - Testar ExcluirMetaUseCase com meta de outro usuário
    - _Requirements: 5.3, 5.4, 5.5, 5.6, 5.7, 5.8, 5.9, 5.10, 5.11, 5.12, 5.13_

- [ ] 10. Adicionar Endpoints no MetaFinanceiraController
  - [ ] 10.1 Implementar GET /metas/{id}
    - Adicionar endpoint no controller
    - Implementar autenticação JWT
    - Implementar validação de UUID
    - Implementar tratamento de erros
    - _Requirements: 5.5, 5.12, 5.13_
  
  - [ ] 10.2 Implementar PUT /metas/{id}
    - Adicionar endpoint no controller
    - Implementar autenticação JWT
    - Implementar validação de request body
    - Implementar tratamento de erros
    - _Requirements: 5.3, 5.7, 5.13_
  
  - [ ] 10.3 Implementar PATCH /metas/{id}/progresso
    - Adicionar endpoint no controller
    - Implementar autenticação JWT
    - Implementar validação de request body
    - Implementar tratamento de erros
    - _Requirements: 5.6, 5.8, 5.9, 5.10, 5.13_
  
  - [ ] 10.4 Implementar DELETE /metas/{id}
    - Adicionar endpoint no controller
    - Implementar autenticação JWT
    - Implementar validação de UUID
    - Implementar tratamento de erros
    - _Requirements: 5.4, 5.11, 5.13_
  
  - [ ] 10.5 Escrever testes de integração para novos endpoints
    - Testar GET /metas/{id} com ID válido
    - Testar GET /metas/{id} com ID inválido (404)
    - Testar GET /metas/{id} sem autenticação (401)
    - Testar PUT /metas/{id} com dados válidos
    - Testar PUT /metas/{id} com dados inválidos (400)
    - Testar PUT /metas/{id} sem autenticação (401)
    - Testar PATCH /metas/{id}/progresso com valor válido
    - Testar PATCH /metas/{id}/progresso sem autenticação (401)
    - Testar DELETE /metas/{id} com ID válido
    - Testar DELETE /metas/{id} sem autenticação (401)
    - _Requirements: 5.3, 5.4, 5.5, 5.6, 5.13_

### Priority 3: Integration and Validation (Medium)

- [ ] 11. Validação End-to-End
  - [ ] 11.1 Testar fluxo completo de dashboard
    - Criar usuário via API
    - Criar transações via API
    - Criar orçamentos via API
    - Criar metas via API
    - Acessar dashboard e validar dados
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6_
  
  - [ ] 11.2 Testar fluxo completo de importação
    - Criar usuário via API
    - Fazer upload de planilha válida
    - Validar que transações foram criadas
    - Validar que dadosIniciaisCarregados foi marcado
    - Acessar dashboard e validar dados importados
    - _Requirements: 2.1, 2.5, 2.9, 2.10, 2.11_
  
  - [ ] 11.3 Testar fluxo completo de CRUD de orçamentos
    - Criar orçamento via API
    - Listar orçamentos via API
    - Obter orçamento por ID via API
    - Atualizar orçamento via API
    - Excluir orçamento via API
    - Validar que orçamento foi soft-deleted
    - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5, 4.7_
  
  - [ ] 11.4 Testar fluxo completo de CRUD de metas
    - Criar meta via API
    - Listar metas via API
    - Obter meta por ID via API
    - Atualizar meta via API
    - Atualizar progresso via API
    - Validar recálculo de percentual
    - Excluir meta via API
    - Validar que meta foi soft-deleted
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.8, 5.11_

- [ ] 12. Testes de Performance
  - [ ] 12.1 Testar performance do dashboard
    - Testar com 1000 transações
    - Testar com 50 orçamentos
    - Testar com 50 metas
    - Validar tempo de resposta < 1 segundo
    - _Requirements: 1.1_
  
  - [ ] 12.2 Testar performance da importação
    - Testar importação de 1000 linhas
    - Testar importação de 5000 linhas
    - Validar tempo de processamento aceitável
    - Validar uso de memória
    - _Requirements: 2.1, 2.14_

- [ ] 13. Documentação e Finalização
  - [ ] 13.1 Atualizar documentação da API
    - Documentar endpoint GET /dashboard/resumo
    - Documentar endpoint POST /importacao/upload
    - Documentar novos endpoints de orçamentos
    - Documentar novos endpoints de metas
    - Atualizar exemplos de requisições e respostas
  
  - [ ] 13.2 Atualizar README.md
    - Adicionar informações sobre dashboard
    - Adicionar informações sobre importação
    - Adicionar exemplos de uso
    - Atualizar checklist de funcionalidades
  
  - [ ] 13.3 Atualizar TESTING_STATUS.md
    - Marcar dashboard como implementado
    - Marcar importação como implementado
    - Marcar CRUD completo de orçamentos como implementado
    - Marcar CRUD completo de metas como implementado
    - Atualizar status de deployment readiness

## Notes

### Backend
- Todas as tarefas devem seguir a arquitetura hexagonal existente
- Todos os endpoints devem usar autenticação JWT
- Todos os endpoints devem validar propriedade de recursos
- Todos os use cases devem ter testes unitários com >80% de cobertura
- Todos os controllers devem ter testes de integração
- Seguir padrões de código existentes no projeto
- Usar mesmas convenções de nomenclatura
- Manter consistência com DTOs e responses existentes
- Todas as operações de banco devem ser transacionais
- Preservar audit trail onde aplicável
- Implementar soft delete para exclusões

### Frontend (UI/UX)
- **shadcn/ui**: Usar para componentes interativos (forms, modals, dropdowns)
- **DaisyUI**: Usar para cards, badges, alerts, stats
- **TailwindCSS**: Usar para layout, spacing, cores, responsividade
- Seguir design system definido (cores, tipografia, spacing)
- Implementar estados: loading, empty, error, success
- Garantir responsividade (mobile, tablet, desktop)
- Incluir acessibilidade (ARIA labels, keyboard navigation)
- Usar animações sutis (transitions)
- Manter consistência visual em toda aplicação
- Consultar `UI_UX_GUIDELINES.md` para padrões detalhados

## Success Criteria

A implementação será considerada completa quando:

1. ✅ Todos os endpoints Priority 1 estão implementados e testados
2. ✅ Todos os endpoints Priority 2 estão implementados e testados
3. ✅ Frontend dashboard page carrega sem erros (sem 500)
4. ✅ Frontend import page funciona completamente
5. ✅ Todos os testes unitários passam
6. ✅ Todos os testes de integração passam
7. ✅ Cobertura de testes >90% para código novo
8. ✅ Testes manuais confirmam funcionalidade end-to-end
9. ✅ Documentação está atualizada
10. ✅ Aplicação está pronta para deployment em produção

## Estimated Effort

- **Priority 1 (Dashboard + Import)**: 3-4 dias
  - Dashboard: 1-1.5 dias
  - Import: 2-2.5 dias

- **Priority 2 (Complete CRUD)**: 2-3 dias
  - Budget CRUD: 1-1.5 dias
  - Goal CRUD: 1-1.5 dias

- **Priority 3 (Integration + Validation)**: 1-2 dias
  - End-to-end tests: 0.5-1 dia
  - Performance tests: 0.5 dia
  - Documentation: 0.5 dia

**Total Estimated Effort**: 6-9 dias de desenvolvimento
