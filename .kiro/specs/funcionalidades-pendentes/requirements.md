# Requirements Document: Funcionalidades Pendentes

## Introduction

Este documento especifica as funcionalidades que ainda precisam ser implementadas no Sistema de Gestão Financeira Doméstica para completar a aplicação conforme o design original. As funcionalidades pendentes incluem endpoints críticos para dashboard e importação de planilhas, além de operações CRUD completas para transações, orçamentos e metas.

## Glossary

- **DashboardController**: Controller REST responsável por agregar dados financeiros do usuário
- **ImportController**: Controller REST responsável por processar upload de planilhas
- **CRUD Completo**: Create, Read, Update, Delete operations para uma entidade
- **Resumo Financeiro**: Agregação de dados incluindo saldo, receitas, despesas, orçamentos e metas
- **Planilha Financeira**: Arquivo Excel (.xlsx) ou CSV contendo transações históricas

## Requirements

### Requirement 1: Dashboard Summary Endpoint

**User Story:** Como um usuário autenticado, eu quero acessar um endpoint de resumo do dashboard, para que eu possa visualizar minha situação financeira consolidada em uma única requisição.

#### Acceptance Criteria

1. WHEN a user requests `/dashboard/resumo`, THE Sistema SHALL return a consolidated financial summary
2. THE summary SHALL include current balance (sum of all active transactions)
3. THE summary SHALL include monthly income (sum of RECEITA transactions in current month)
4. THE summary SHALL include monthly expenses (sum of DESPESA transactions in current month)
5. THE summary SHALL include status of all active budgets with spending percentage
6. THE summary SHALL include progress of all active financial goals
7. THE summary SHALL include the 10 most recent transactions
8. THE summary SHALL include investment portfolio summary if investments exist
9. WHEN the user has no data, THE Sistema SHALL return zero values and empty lists
10. THE endpoint SHALL require JWT authentication
11. THE endpoint SHALL only return data for the authenticated user

### Requirement 2: Spreadsheet Import Endpoint

**User Story:** Como um usuário autenticado, eu quero fazer upload de uma planilha financeira, para que o sistema importe minhas transações históricas automaticamente.

#### Acceptance Criteria

1. WHEN a user uploads a file to `/importacao/upload`, THE Sistema SHALL accept Excel (.xlsx) and CSV formats
2. THE Sistema SHALL validate file format before processing
3. THE Sistema SHALL parse the spreadsheet and extract transaction data
4. THE Sistema SHALL validate that each transaction has required fields (date, amount, description, category)
5. THE Sistema SHALL detect potential duplicate transactions based on date, amount, and description similarity
6. WHEN duplicates are detected, THE Sistema SHALL flag them in the response but not reject the import
7. THE Sistema SHALL validate transaction data (positive amounts, valid dates, valid categories)
8. WHEN validation fails, THE Sistema SHALL return detailed error messages for each invalid row
9. WHEN import is successful, THE Sistema SHALL save all valid transactions
10. THE Sistema SHALL mark the user's `dadosIniciaisCarregados` flag as true after first successful import
11. THE Sistema SHALL return import results including: total rows, successful imports, failed imports, duplicates detected
12. THE endpoint SHALL require JWT authentication
13. THE endpoint SHALL only import data for the authenticated user
14. THE Sistema SHALL limit file size to 10MB maximum

### Requirement 3: Complete Transaction CRUD Operations

**User Story:** Como um usuário autenticado, eu quero ter operações completas de criação, leitura, atualização e exclusão de transações, para que eu possa gerenciar completamente meus registros financeiros.

#### Acceptance Criteria

1. ✅ CREATE: `POST /transacoes` - Already implemented
2. ✅ READ: `GET /transacoes` - Already implemented
3. ✅ UPDATE: `PUT /transacoes/{id}` - Already implemented
4. ✅ DELETE: `DELETE /transacoes/{id}` - Already implemented (soft delete)
5. ✅ REACTIVATE: `PATCH /transacoes/{id}/reativar` - Already implemented
6. All operations SHALL require JWT authentication
7. All operations SHALL only affect transactions owned by the authenticated user

### Requirement 4: Complete Budget CRUD Operations

**User Story:** Como um usuário autenticado, eu quero ter operações completas de criação, leitura, atualização e exclusão de orçamentos, para que eu possa gerenciar completamente meus limites de gastos.

#### Acceptance Criteria

1. ✅ CREATE: `POST /orcamentos` - Already implemented
2. ✅ READ: `GET /orcamentos` - Already implemented
3. ❌ UPDATE: `PUT /orcamentos/{id}` - **NOT IMPLEMENTED**
4. ❌ DELETE: `DELETE /orcamentos/{id}` - **NOT IMPLEMENTED**
5. ❌ GET BY ID: `GET /orcamentos/{id}` - **NOT IMPLEMENTED**
6. WHEN updating a budget, THE Sistema SHALL preserve audit trail
7. WHEN deleting a budget, THE Sistema SHALL perform soft delete (mark as inactive)
8. WHEN getting budget by ID, THE Sistema SHALL return 404 if not found or not owned by user
9. All operations SHALL require JWT authentication
10. All operations SHALL only affect budgets owned by the authenticated user

### Requirement 5: Complete Goal CRUD Operations

**User Story:** Como um usuário autenticado, eu quero ter operações completas de criação, leitura, atualização e exclusão de metas financeiras, para que eu possa gerenciar completamente meus objetivos financeiros.

#### Acceptance Criteria

1. ✅ CREATE: `POST /metas` - Already implemented
2. ✅ READ: `GET /metas` - Already implemented
3. ❌ UPDATE: `PUT /metas/{id}` - **NOT IMPLEMENTED**
4. ❌ DELETE: `DELETE /metas/{id}` - **NOT IMPLEMENTED**
5. ❌ GET BY ID: `GET /metas/{id}` - **NOT IMPLEMENTED**
6. ❌ UPDATE PROGRESS: `PATCH /metas/{id}/progresso` - **NOT IMPLEMENTED**
7. WHEN updating a goal, THE Sistema SHALL preserve audit trail
8. WHEN updating progress, THE Sistema SHALL recalculate completion percentage
9. WHEN updating progress, THE Sistema SHALL recalculate estimated completion date
10. WHEN a goal reaches 100% completion, THE Sistema SHALL automatically mark it as CONCLUIDA
11. WHEN deleting a goal, THE Sistema SHALL perform soft delete (mark as inactive)
12. WHEN getting goal by ID, THE Sistema SHALL return 404 if not found or not owned by user
13. All operations SHALL require JWT authentication
14. All operations SHALL only affect goals owned by the authenticated user

### Requirement 6: Investment Portfolio Management (Optional - Future)

**User Story:** Como um usuário autenticado, eu quero gerenciar minha carteira de investimentos, para que eu possa acompanhar a performance dos meus ativos.

#### Acceptance Criteria

1. ❌ CREATE: `POST /investimentos` - **NOT IMPLEMENTED**
2. ❌ READ: `GET /investimentos` - **NOT IMPLEMENTED**
3. ❌ UPDATE: `PUT /investimentos/{id}` - **NOT IMPLEMENTED**
4. ❌ DELETE: `DELETE /investimentos/{id}` - **NOT IMPLEMENTED**
5. ❌ GET PORTFOLIO SUMMARY: `GET /investimentos/resumo` - **NOT IMPLEMENTED**
6. THE Sistema SHALL support multiple investment types (stocks, bonds, funds, crypto)
7. THE Sistema SHALL calculate portfolio value based on current market prices (read-only)
8. THE Sistema SHALL display individual asset performance (gain/loss, percentage)
9. THE Sistema SHALL clearly indicate that values are for evaluation only
10. All operations SHALL require JWT authentication

### Requirement 7: AI Insights and Recommendations (Optional - Future)

**User Story:** Como um usuário autenticado, eu quero receber insights e recomendações da IA sobre minhas finanças, para que eu possa tomar decisões mais informadas.

#### Acceptance Criteria

1. ❌ GET INSIGHTS: `GET /insights` - **NOT IMPLEMENTED**
2. ❌ GENERATE INSIGHTS: `POST /insights/gerar` - **NOT IMPLEMENTED**
3. ❌ CONFIRM INSIGHT: `POST /insights/{id}/confirmar` - **NOT IMPLEMENTED**
4. THE IA_Assessora SHALL analyze spending patterns and provide recommendations
5. THE IA_Assessora SHALL identify potential savings opportunities
6. THE IA_Assessora SHALL explain the reasoning behind each recommendation
7. THE Sistema SHALL never execute financial actions based solely on IA recommendations
8. All operations SHALL require JWT authentication

## Priority Classification

### Priority 1 (Critical - Blocking User Experience)
- Requirement 1: Dashboard Summary Endpoint
- Requirement 2: Spreadsheet Import Endpoint

### Priority 2 (High - Complete Core Features)
- Requirement 4: Complete Budget CRUD Operations
- Requirement 5: Complete Goal CRUD Operations

### Priority 3 (Medium - Already Working)
- Requirement 3: Complete Transaction CRUD Operations (✅ Already complete)

### Priority 4 (Low - Future Enhancements)
- Requirement 6: Investment Portfolio Management
- Requirement 7: AI Insights and Recommendations

## Technical Constraints

1. All endpoints must follow REST conventions
2. All endpoints must use JWT authentication via Spring Security
3. All endpoints must validate user ownership of resources
4. All endpoints must return appropriate HTTP status codes
5. All endpoints must handle errors gracefully with descriptive messages
6. File uploads must be limited to 10MB
7. Spreadsheet parsing must support Excel (.xlsx) and CSV formats
8. All database operations must be transactional
9. All operations must maintain audit trails where applicable
10. Frontend already expects these endpoints - backend must match existing contracts

## Success Criteria

The implementation will be considered complete when:

1. ✅ All Priority 1 endpoints are implemented and tested
2. ✅ All Priority 2 endpoints are implemented and tested
3. ✅ Frontend dashboard page loads without errors
4. ✅ Frontend import page successfully uploads and processes files
5. ✅ All CRUD operations work end-to-end from frontend to database
6. ✅ All endpoints have unit tests with >80% coverage
7. ✅ All endpoints have integration tests
8. ✅ Manual testing confirms all features work as expected
9. ✅ No 500 errors in browser console
10. ✅ Application is ready for production deployment
