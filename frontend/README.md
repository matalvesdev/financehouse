# Frontend - Sistema de Gestão Financeira Doméstica

Este é o frontend da aplicação de gestão financeira doméstica, construído com React, TypeScript e Vite.

## 🚀 Tecnologias

- **React 18** - Biblioteca para interfaces de usuário
- **TypeScript** - Superset tipado do JavaScript
- **Vite** - Build tool e dev server
- **TailwindCSS** - Framework CSS utilitário
- **shadcn/ui** - Componentes de UI reutilizáveis
- **React Router DOM** - Roteamento
- **Zustand** - Gerenciamento de estado
- **Zod** - Validação de schemas
- **React Hook Form** - Gerenciamento de formulários
- **Axios** - Cliente HTTP
- **Vitest** - Framework de testes
- **React Testing Library** - Utilitários de teste

## 📁 Estrutura do Projeto

```
src/
├── components/          # Componentes React
│   ├── ui/             # Componentes de UI base (shadcn/ui)
│   ├── Layout.tsx      # Layout principal
│   ├── PrivateRoute.tsx # Proteção de rotas
│   └── ConfirmDialog.tsx # Dialog de confirmação
├── lib/                # Utilitários e configurações
│   ├── api.ts          # Cliente HTTP e services
│   ├── schemas.ts      # Schemas de validação Zod
│   └── utils.ts        # Funções utilitárias
├── pages/              # Páginas da aplicação
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── DashboardPage.tsx
│   ├── TransactionsPage.tsx
│   ├── BudgetsPage.tsx
│   ├── GoalsPage.tsx
│   └── ImportPage.tsx
├── stores/             # Stores Zustand
│   ├── authStore.ts    # Estado de autenticação
│   ├── transactionStore.ts
│   ├── budgetStore.ts
│   ├── goalStore.ts
│   ├── importStore.ts
│   ├── dashboardStore.ts
│   └── confirmStore.ts
├── types/              # Definições de tipos TypeScript
│   └── index.ts
└── test/               # Configurações de teste
    └── setup.ts
```

## 🛠️ Scripts Disponíveis

```bash
# Desenvolvimento
npm run dev              # Inicia servidor de desenvolvimento

# Build
npm run build           # Build para produção
npm run preview         # Preview do build

# Testes
npm test                # Executa testes em modo watch
npm run test:ui         # Interface gráfica dos testes

# Linting
npm run lint            # Executa ESLint
```

## 🔧 Configuração

### Variáveis de Ambiente

Crie um arquivo `.env.local` baseado no `.env.example`:

```env
VITE_API_URL=http://localhost:8080/api
```

### shadcn/ui

O projeto está configurado com shadcn/ui. Para adicionar novos componentes:

```bash
npx shadcn-ui@latest add [component-name]
```

## 🎨 Componentes UI Disponíveis

- **Button** - Botões com variantes e estados
- **Input** - Campos de entrada com validação
- **Select** - Seleção com opções
- **Modal** - Modais reutilizáveis
- **Card** - Cards para conteúdo
- **Badge** - Badges para status
- **Progress** - Barras de progresso

## 🔐 Autenticação

O sistema utiliza JWT tokens com refresh automático:

- **Access Token** - Token de acesso (curta duração)
- **Refresh Token** - Token para renovação (longa duração)
- **Interceptors** - Renovação automática de tokens
- **Proteção de Rotas** - Rotas protegidas por autenticação

## 📊 Gerenciamento de Estado

Utiliza Zustand para gerenciamento de estado com:

- **Persistência** - Estado persistido no localStorage
- **Tipagem** - Totalmente tipado com TypeScript
- **Modularização** - Stores separadas por domínio

## ✅ Validação

Utiliza Zod para validação de formulários e dados:

- **Schemas** - Definições de validação reutilizáveis
- **Integração** - Integrado com React Hook Form
- **Tipagem** - Types inferidos automaticamente

## 🧪 Testes

- **Vitest** - Framework de testes rápido
- **React Testing Library** - Testes focados no usuário
- **Cobertura** - Testes unitários e de integração
- **Mocks** - Mocks para APIs e stores

## 🚀 Deploy

O projeto está configurado para deploy com Docker:

```bash
# Build da imagem
docker build -t gestao-financeira-frontend .

# Executar container
docker run -p 3000:80 gestao-financeira-frontend
```

## 📝 Convenções

- **Componentes** - PascalCase
- **Arquivos** - PascalCase para componentes, camelCase para utilitários
- **Imports** - Absolute imports com alias `@/`
- **Styling** - TailwindCSS com classes utilitárias
- **Commits** - Conventional Commits