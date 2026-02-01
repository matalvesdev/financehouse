# 🧪 Guia de Testes Locais

Este guia mostra como rodar a aplicação completa localmente usando Docker Compose antes de fazer o deploy na Oracle Cloud.

## 📋 Pré-requisitos

- ✅ Docker Desktop instalado e rodando
- ✅ Git instalado
- ✅ Portas disponíveis: 3000 (frontend), 8080 (backend), 5432 (postgres), 5050 (pgadmin)

## 🚀 Passo 1: Verificar Docker

```powershell
# Verificar se Docker está rodando
docker --version
docker-compose --version

# Verificar se Docker Desktop está ativo
docker ps
```

Se o Docker não estiver rodando, abra o **Docker Desktop** e aguarde inicializar.

## 🔧 Passo 2: Preparar Ambiente

### 2.1 Criar arquivo .env (opcional)

Crie o arquivo `.env` na raiz do projeto (já existe `.env.example` como referência):

```env
# Database
POSTGRES_DB=gestao_financeira
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

# Backend
JWT_SECRET=mySecretKey123456789012345678901234567890
ENCRYPTION_KEY=myEncryptionKey12345678901234567890

# Frontend
VITE_API_URL=http://localhost:8080/api
```

### 2.2 Verificar Dockerfiles

Os Dockerfiles de desenvolvimento já estão configurados:
- `backend/Dockerfile.dev` - Backend Spring Boot
- `frontend/Dockerfile.dev` - Frontend React

## 🏃 Passo 3: Iniciar Aplicação

### Opção 1: Iniciar Tudo de Uma Vez

```powershell
# Na raiz do projeto
docker-compose up --build
```

Isso irá:
1. Construir as imagens Docker
2. Iniciar PostgreSQL
3. Iniciar Backend (Spring Boot)
4. Iniciar Frontend (React + Vite)
5. Iniciar pgAdmin (opcional)

### Opção 2: Iniciar em Background

```powershell
docker-compose up -d --build
```

Para ver os logs:
```powershell
docker-compose logs -f
```

Para ver logs de um serviço específico:
```powershell
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f postgres
```

## ⏱️ Passo 4: Aguardar Inicialização

A aplicação leva alguns minutos para inicializar:

1. **PostgreSQL** (~10 segundos): Banco de dados
2. **Backend** (~2-3 minutos): Spring Boot + Flyway migrations
3. **Frontend** (~30 segundos): Vite dev server

### Verificar Status

```powershell
# Ver containers rodando
docker-compose ps

# Verificar saúde dos containers
docker ps
```

Você deve ver algo como:
```
NAME                              STATUS
gestao-financeira-backend         Up (healthy)
gestao-financeira-db              Up (healthy)
gestao-financeira-frontend        Up
gestao-financeira-pgadmin         Up
```

## 🌐 Passo 5: Acessar Aplicação

### Frontend (Interface do Usuário)
- URL: http://localhost:3000
- Página de login/registro deve aparecer

### Backend (API REST)
- URL: http://localhost:8080
- Health Check: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html

### pgAdmin (Gerenciador de Banco de Dados)
- URL: http://localhost:5050
- Email: `admin@gestaofinanceira.com`
- Senha: `admin`

Para conectar ao banco no pgAdmin:
- Host: `postgres`
- Port: `5432`
- Database: `gestao_financeira`
- Username: `postgres`
- Password: `postgres`

## ✅ Passo 6: Testar Funcionalidades

### 6.1 Criar Conta

1. Acesse: http://localhost:3000
2. Clique em "Criar Conta" ou "Registrar"
3. Preencha:
   - Nome: Seu Nome
   - Email: teste@exemplo.com
   - Senha: Senha123!
4. Clique em "Registrar"

### 6.2 Fazer Login

1. Use as credenciais criadas
2. Você deve ser redirecionado para o Dashboard

### 6.3 Testar Funcionalidades

- ✅ **Dashboard**: Ver resumo financeiro
- ✅ **Transações**: Criar, editar, excluir transações
- ✅ **Orçamentos**: Criar e gerenciar orçamentos
- ✅ **Metas**: Criar e acompanhar metas financeiras
- ✅ **Importação**: Importar planilha CSV/Excel

### 6.4 Testar API Diretamente

```powershell
# Health Check
curl http://localhost:8080/actuator/health

# Criar usuário via API
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"nome\":\"Teste\",\"email\":\"api@teste.com\",\"senha\":\"Senha123!\"}'

# Login via API
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"api@teste.com\",\"senha\":\"Senha123!\"}'
```

## 🧪 Passo 7: Executar Testes

### Testes do Backend

```powershell
# Entrar no container do backend
docker-compose exec backend bash

# Executar todos os testes
./mvnw test

# Executar apenas testes unitários
./mvnw test -Dtest=*Test

# Executar apenas testes de integração
./mvnw test -Dtest=*IntegrationTest

# Executar testes de propriedade (Property-Based Tests)
./mvnw test -Dtest=*PropertyTest

# Sair do container
exit
```

### Testes do Frontend

```powershell
# Entrar no container do frontend
docker-compose exec frontend sh

# Executar todos os testes
npm test

# Executar testes com cobertura
npm run test:coverage

# Sair do container
exit
```

## 🔍 Passo 8: Verificar Logs

### Ver Logs em Tempo Real

```powershell
# Todos os serviços
docker-compose logs -f

# Apenas backend
docker-compose logs -f backend

# Apenas frontend
docker-compose logs -f frontend

# Apenas banco de dados
docker-compose logs -f postgres
```

### Verificar Erros

```powershell
# Últimas 100 linhas do backend
docker-compose logs --tail=100 backend

# Buscar por erros
docker-compose logs backend | Select-String -Pattern "ERROR"
docker-compose logs backend | Select-String -Pattern "Exception"
```

## 🛑 Passo 9: Parar Aplicação

### Parar containers (mantém dados)

```powershell
docker-compose stop
```

### Parar e remover containers (mantém volumes)

```powershell
docker-compose down
```

### Parar e remover TUDO (incluindo dados do banco)

```powershell
docker-compose down -v
```

## 🔄 Passo 10: Reiniciar Aplicação

### Reiniciar tudo

```powershell
docker-compose restart
```

### Reiniciar apenas um serviço

```powershell
docker-compose restart backend
docker-compose restart frontend
```

### Rebuild e reiniciar

```powershell
docker-compose up -d --build
```

## 🐛 Troubleshooting

### Problema: Porta já em uso

```
Error: bind: address already in use
```

**Solução**: Verificar qual processo está usando a porta

```powershell
# Verificar porta 8080 (backend)
netstat -ano | findstr :8080

# Verificar porta 3000 (frontend)
netstat -ano | findstr :3000

# Verificar porta 5432 (postgres)
netstat -ano | findstr :5432

# Matar processo (substitua PID pelo número encontrado)
taskkill /PID <PID> /F
```

### Problema: Backend não conecta ao banco

**Sintomas**: Erro "Connection refused" ou "Unknown host"

**Solução**:
1. Verificar se postgres está rodando: `docker-compose ps`
2. Verificar logs do postgres: `docker-compose logs postgres`
3. Reiniciar: `docker-compose restart postgres backend`

### Problema: Frontend não carrega

**Sintomas**: Página em branco ou erro de conexão

**Solução**:
1. Verificar logs: `docker-compose logs frontend`
2. Verificar se backend está rodando: `curl http://localhost:8080/actuator/health`
3. Limpar cache do navegador (Ctrl+Shift+Delete)
4. Rebuild: `docker-compose up -d --build frontend`

### Problema: Mudanças no código não aparecem

**Backend**:
```powershell
docker-compose restart backend
```

**Frontend**:
- O Vite tem hot-reload automático
- Se não funcionar: `docker-compose restart frontend`

### Problema: Banco de dados corrompido

**Solução**: Resetar banco de dados

```powershell
# Parar tudo
docker-compose down

# Remover volume do banco
docker volume rm financehouse_postgres_data

# Iniciar novamente
docker-compose up -d
```

### Problema: Erro de memória/performance

**Solução**: Aumentar recursos do Docker Desktop

1. Abrir Docker Desktop
2. Settings → Resources
3. Aumentar:
   - Memory: 4GB ou mais
   - CPUs: 2 ou mais
4. Apply & Restart

## 📊 Monitoramento

### Ver uso de recursos

```powershell
docker stats
```

### Ver espaço em disco

```powershell
docker system df
```

### Limpar recursos não utilizados

```powershell
# Limpar containers parados
docker container prune

# Limpar imagens não utilizadas
docker image prune

# Limpar tudo (cuidado!)
docker system prune -a
```

## ✅ Checklist de Testes

Antes de fazer deploy, verifique:

- [ ] Frontend carrega em http://localhost:3000
- [ ] Backend responde em http://localhost:8080/actuator/health
- [ ] Consegue criar conta
- [ ] Consegue fazer login
- [ ] Consegue criar transação
- [ ] Consegue criar orçamento
- [ ] Consegue criar meta financeira
- [ ] Dashboard mostra dados corretamente
- [ ] Importação de planilha funciona
- [ ] Todos os testes do backend passam
- [ ] Todos os testes do frontend passam
- [ ] Não há erros críticos nos logs

## 🎯 Próximos Passos

Depois de testar localmente e confirmar que tudo funciona:

1. ✅ Testes locais completos
2. 🚀 Deploy na Oracle Cloud (seguir guia de deploy)
3. 🔒 Configurar HTTPS
4. 📊 Configurar monitoramento
5. 🔄 Configurar CI/CD

---

## 📝 Comandos Úteis Resumidos

```powershell
# Iniciar
docker-compose up -d --build

# Ver logs
docker-compose logs -f

# Ver status
docker-compose ps

# Parar
docker-compose down

# Reiniciar
docker-compose restart

# Executar testes backend
docker-compose exec backend ./mvnw test

# Executar testes frontend
docker-compose exec frontend npm test

# Limpar tudo
docker-compose down -v
docker system prune -a
```

---

**Boa sorte com os testes!** 🚀

Se encontrar algum problema, consulte a seção de Troubleshooting ou verifique os logs.


## Recent Fixes

### Authentication Fix (2026-02-01)

**Issue**: Login was failing with "Credenciais inválidas" even with correct credentials.

**Root Cause**: The email field was encrypted in the database using `EncryptedStringConverter`, which prevented the login query from finding users by email (you can't search encrypted fields).

**Solution**: Removed encryption from the email field in `UsuarioJpaEntity.java`. Email addresses don't need to be encrypted at rest since they're used for login and are not considered highly sensitive. The nome (name) field remains encrypted for privacy.

**Changes Made**:
1. Removed `@Convert(converter = EncryptedStringConverter.class)` from email field
2. Fixed parameter order in `UsuarioResponse` creation (nome and email were swapped)
3. Cleared database and restarted with fresh schema

**Testing**: 
- ✅ User registration works
- ✅ User login works and returns JWT tokens
- ✅ Password verification works correctly

## Next Steps

After confirming everything works locally:
1. Commit and push changes to GitHub
2. Deploy to Oracle Cloud using Terraform (see `terraform/README.md`)
3. Configure production environment variables
4. Run database migrations on production
