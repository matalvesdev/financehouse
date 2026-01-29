# Sistema de Gestão Financeira Doméstica

Sistema web fullstack de gestão financeira doméstica implementado com arquitetura hexagonal e Domain-Driven Design.

## 🚀 Deploy Rápido

### Oracle Cloud Infrastructure (Always Free)

Deploy gratuito na OCI com recursos Always Free (custo zero permanente):

```bash
cd terraform
chmod +x setup.sh
./setup.sh
make full-deploy
```

**Documentação completa**: [OCI_DEPLOYMENT.md](OCI_DEPLOYMENT.md)

**Recursos incluídos**:
- ✅ Compute Instance (1 OCPU, 1GB RAM)
- ✅ Autonomous Database (20GB)
- ✅ Network Infrastructure completa
- ✅ **Custo: R$ 0,00/mês**

## Tecnologias

### Backend
- **Java 21** com Spring Boot 3.x
- **PostgreSQL** para persistência
- **Flyway** para migrations
- **JWT** para autenticação
- **jqwik** para property-based testing
- **Testcontainers** para testes de integração

### Frontend
- **React 18** com TypeScript
- **Vite** como build tool
- **TailwindCSS** para estilização
- **Zustand** para gerenciamento de estado
- **React Hook Form** + **Zod** para formulários
- **Axios** para comunicação HTTP

### Infrastructure
- **Docker** & **Docker Compose** para containerização
- **Terraform** para Infrastructure as Code
- **Oracle Cloud Infrastructure** (OCI) para hosting
- **GitHub Actions** para CI/CD

## Arquitetura

O sistema segue os princípios de **Arquitetura Hexagonal** (Ports & Adapters) com **Domain-Driven Design**:

```
├── Domain Layer (Centro)
│   ├── Entities (Usuario, Transacao, Orcamento, etc.)
│   ├── Value Objects (Email, Valor, Categoria, etc.)
│   └── Domain Services
├── Application Layer (Orquestração)
│   ├── Use Cases
│   ├── Ports (Interfaces)
│   └── DTOs
├── Infrastructure Layer (Adaptadores)
│   ├── Repository Implementations
│   ├── External API Adapters
│   └── File Processing
└── Web Layer (Interface)
    ├── REST Controllers
    ├── Security Configuration
    └── Exception Handling
```

## Princípios Arquiteturais

- **Human-in-the-loop**: Toda ação requer confirmação explícita do usuário
- **Decision ≠ Action**: Sistema separa claramente recomendações de execuções
- **Domain First**: Lógica de negócio isolada de detalhes técnicos
- **Backend Soberano**: Backend governa todo o estado da aplicação
- **Frontend Orquestrador**: Frontend apenas orquestra decisões do usuário

## Desenvolvimento Local

### Pré-requisitos
- Docker e Docker Compose
- Java 21 (para desenvolvimento local sem Docker)
- Node.js 18+ (para desenvolvimento local sem Docker)

### Executando com Docker

1. Clone o repositório
2. Execute o ambiente completo:
```bash
docker-compose up -d
```

Isso iniciará:
- **PostgreSQL** na porta 5432
- **Backend** na porta 8080
- **Frontend** na porta 3000
- **pgAdmin** na porta 5050 (opcional)

### URLs de Acesso

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080/api
- **pgAdmin**: http://localhost:5050
  - Email: admin@gestaofinanceira.com
  - Senha: admin

### Desenvolvimento Local (sem Docker)

#### Backend
```bash
cd backend
./mvnw spring-boot:run
```

#### Frontend
```bash
cd frontend
npm install
npm run dev
```

#### Banco de Dados
Execute PostgreSQL localmente ou use Docker:
```bash
docker run --name postgres-dev -e POSTGRES_DB=gestao_financeira -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15-alpine
```

## Testes

### Backend
```bash
cd backend
./mvnw test                    # Testes unitários
./mvnw test -Dtest=**/*IT      # Testes de integração
./mvnw test -Dtest=**/*Property # Testes de propriedade
```

### Frontend
```bash
cd frontend
npm run test                   # Testes unitários
npm run test:ui               # Interface de testes
```

## Estrutura do Projeto

```
├── backend/                   # Aplicação Spring Boot
│   ├── src/main/java/com/gestaofinanceira/
│   │   ├── domain/           # Camada de Domínio
│   │   ├── application/      # Camada de Aplicação
│   │   ├── infrastructure/   # Camada de Infraestrutura
│   │   └── web/             # Camada Web
│   └── src/main/resources/
│       └── db/migration/    # Scripts Flyway
├── frontend/                 # Aplicação React
│   ├── src/
│   │   ├── components/      # Componentes reutilizáveis
│   │   ├── pages/          # Páginas da aplicação
│   │   ├── stores/         # Gerenciamento de estado
│   │   └── types/          # Definições TypeScript
└── docker-compose.yml       # Orquestração dos serviços
```

## Funcionalidades Principais

- ✅ **Autenticação JWT** com refresh tokens
- ✅ **Importação de planilhas** Excel/CSV
- ✅ **Gestão de transações** com categorização automática
- ✅ **Dashboard financeiro** com métricas em tempo real
- ✅ **Orçamentos** com alertas de limite
- ✅ **Metas financeiras** com tracking de progresso
- ✅ **Carteira de investimentos** (somente visualização)
- ✅ **Insights com IA** (recomendações, não execução)
- ✅ **Sistema de confirmação** para todas as ações

## Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT. Veja o arquivo `LICENSE` para mais detalhes.