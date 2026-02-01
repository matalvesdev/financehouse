# UI/UX Guidelines - Funcionalidades Pendentes

## 🎨 Design System

**⚠️ IMPORTANTE**: Este projeto segue um design system customizado minimalista e moderno.

**Consulte o arquivo completo**: [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md)

### Resumo do Design System

- **Fonte**: DM Sans (Google Fonts)
- **Paleta**: Verde (#4CAF50), Preto (#212121), Branco (#FFFFFF)
- **Status**: Verde (sucesso), Laranja (atenção), Vermelho (erro)
- **Estilo**: Minimalista, moderno, clean
- **Inspiração**: Dribbble e Behance (financial dashboards)

## 🎨 Stack de UI/UX

O projeto utiliza uma combinação poderosa de bibliotecas para criar uma interface moderna e responsiva:

- **shadcn/ui** - Componentes React reutilizáveis e acessíveis
- **DaisyUI** - Componentes prontos baseados em Tailwind CSS
- **TailwindCSS** - Framework CSS utility-first
- **DM Sans** - Fonte principal do projeto

## 📦 Bibliotecas Instaladas

```json
{
  "dependencies": {
    "tailwindcss": "^3.x",
    "daisyui": "^4.x",
    "@radix-ui/react-*": "^1.x"
  }
}
```

## 🎯 Quando Usar Cada Biblioteca

### shadcn/ui
**Use para**:
- Componentes complexos e interativos
- Formulários com validação
- Modals e dialogs
- Dropdowns e selects
- Tooltips e popovers

**Exemplos**:
```tsx
import { Button } from "@/components/ui/Button"
import { Input } from "@/components/ui/Input"
import { Modal } from "@/components/ui/Modal"
```

### DaisyUI
**Use para**:
- Cards e containers
- Badges e tags
- Alerts e notificações
- Loading states
- Stats e métricas

**Exemplos**:
```tsx
<div className="card bg-base-100 shadow-xl">
  <div className="card-body">
    <h2 className="card-title">Dashboard</h2>
  </div>
</div>
```

### TailwindCSS
**Use para**:
- Layout e spacing
- Cores e tipografia
- Responsividade
- Animações e transições
- Customizações específicas

**Exemplos**:
```tsx
<div className="flex flex-col gap-4 p-6 bg-white rounded-lg shadow-md">
  <h1 className="text-2xl font-bold text-gray-900">Título</h1>
</div>
```

## 🎨 Design System

### Cores

**⚠️ Use as cores do Design System customizado**: [DESIGN_SYSTEM.md](./DESIGN_SYSTEM.md)

```css
/* Primary Colors - Verde */
--primary-500: #4CAF50      /* Verde principal */
--primary-600: #43A047      /* Verde hover */
--primary-700: #388E3C      /* Verde escuro */

/* Status Colors */
--success-500: #4CAF50      /* Verde - sucesso, receita */
--warning-500: #FF9800      /* Laranja - atenção, alerta */
--error-500: #F44336        /* Vermelho - erro, despesa */

/* Neutral Colors */
--neutral-0: #FFFFFF        /* Branco */
--neutral-900: #212121      /* Preto suave */
--neutral-600: #757575      /* Cinza texto secundário */
```

### Tipografia

**Fonte Principal**: DM Sans (Google Fonts)

```css
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&display=swap');

font-family: 'DM Sans', sans-serif;
```

```css
/* Headings */
.text-h1 { @apply text-4xl font-bold }
.text-h2 { @apply text-3xl font-semibold }
.text-h3 { @apply text-2xl font-semibold }
.text-h4 { @apply text-xl font-semibold }

/* Body */
.text-body { @apply text-base }
.text-small { @apply text-sm }
.text-tiny { @apply text-xs }

/* Numbers - Valores monetários */
.text-number { @apply text-2xl font-bold tabular-nums }
```

### Spacing

```css
/* Consistent spacing scale */
gap-2  /* 0.5rem - 8px */
gap-4  /* 1rem - 16px */
gap-6  /* 1.5rem - 24px */
gap-8  /* 2rem - 32px */
```

## 📱 Componentes por Funcionalidade

### 1. Dashboard Page

#### Layout Principal
```tsx
<div className="container mx-auto p-6">
  <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
    {/* Cards de métricas */}
  </div>
</div>
```

#### Card de Métrica (DaisyUI)
```tsx
<div className="stats shadow">
  <div className="stat">
    <div className="stat-title">Saldo Atual</div>
    <div className="stat-value text-primary">R$ 5.000,00</div>
    <div className="stat-desc">↗︎ 400 (22%) este mês</div>
  </div>
</div>
```

#### Card de Orçamento (shadcn/ui + DaisyUI)
```tsx
<div className="card bg-base-100 shadow-xl">
  <div className="card-body">
    <h2 className="card-title">Alimentação</h2>
    <Progress value={70} className="w-full" />
    <div className="flex justify-between text-sm">
      <span>R$ 350,00 / R$ 500,00</span>
      <span className="badge badge-warning">70%</span>
    </div>
  </div>
</div>
```

### 2. Import Page

#### Upload Area (shadcn/ui)
```tsx
<div className="border-2 border-dashed border-gray-300 rounded-lg p-8 text-center hover:border-primary transition-colors">
  <Input
    type="file"
    accept=".xlsx,.xls,.csv"
    className="hidden"
    id="file-upload"
  />
  <label htmlFor="file-upload" className="cursor-pointer">
    <div className="space-y-2">
      <Upload className="mx-auto h-12 w-12 text-gray-400" />
      <p className="text-sm text-gray-600">
        Arraste um arquivo ou clique para selecionar
      </p>
      <p className="text-xs text-gray-500">
        Excel (.xlsx, .xls) ou CSV (máx. 10MB)
      </p>
    </div>
  </label>
</div>
```

#### Preview Table (DaisyUI)
```tsx
<div className="overflow-x-auto">
  <table className="table table-zebra w-full">
    <thead>
      <tr>
        <th>Data</th>
        <th>Descrição</th>
        <th>Valor</th>
        <th>Status</th>
      </tr>
    </thead>
    <tbody>
      {transactions.map(t => (
        <tr key={t.id}>
          <td>{t.date}</td>
          <td>{t.description}</td>
          <td>{t.amount}</td>
          <td>
            <span className="badge badge-success">Válido</span>
          </td>
        </tr>
      ))}
    </tbody>
  </table>
</div>
```

#### Alert de Duplicatas (DaisyUI)
```tsx
<div className="alert alert-warning shadow-lg">
  <div>
    <AlertTriangle className="h-6 w-6" />
    <div>
      <h3 className="font-bold">5 duplicatas potenciais encontradas</h3>
      <div className="text-xs">Revise antes de importar</div>
    </div>
  </div>
  <button className="btn btn-sm">Ver Detalhes</button>
</div>
```

### 3. Budget CRUD

#### Form de Criação/Edição (shadcn/ui)
```tsx
<Form {...form}>
  <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
    <FormField
      control={form.control}
      name="categoria"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Categoria</FormLabel>
          <Select onValueChange={field.onChange} defaultValue={field.value}>
            <FormControl>
              <SelectTrigger>
                <SelectValue placeholder="Selecione uma categoria" />
              </SelectTrigger>
            </FormControl>
            <SelectContent>
              <SelectItem value="ALIMENTACAO">Alimentação</SelectItem>
              <SelectItem value="TRANSPORTE">Transporte</SelectItem>
            </SelectContent>
          </Select>
          <FormMessage />
        </FormItem>
      )}
    />
    
    <FormField
      control={form.control}
      name="limite"
      render={({ field }) => (
        <FormItem>
          <FormLabel>Limite</FormLabel>
          <FormControl>
            <Input
              type="number"
              step="0.01"
              placeholder="0.00"
              {...field}
            />
          </FormControl>
          <FormMessage />
        </FormItem>
      )}
    />
    
    <Button type="submit" className="w-full">
      Salvar Orçamento
    </Button>
  </form>
</Form>
```

#### Lista de Orçamentos (DaisyUI)
```tsx
<div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
  {budgets.map(budget => (
    <div key={budget.id} className="card bg-base-100 shadow-xl">
      <div className="card-body">
        <div className="flex justify-between items-start">
          <h2 className="card-title">{budget.categoria}</h2>
          <div className="dropdown dropdown-end">
            <label tabIndex={0} className="btn btn-ghost btn-sm">
              <MoreVertical className="h-4 w-4" />
            </label>
            <ul tabIndex={0} className="dropdown-content menu p-2 shadow bg-base-100 rounded-box w-52">
              <li><a onClick={() => handleEdit(budget)}>Editar</a></li>
              <li><a onClick={() => handleDelete(budget)}>Excluir</a></li>
            </ul>
          </div>
        </div>
        
        <div className="space-y-2">
          <Progress value={budget.percentual} />
          <div className="flex justify-between text-sm">
            <span>R$ {budget.gastoAtual}</span>
            <span>R$ {budget.limite}</span>
          </div>
        </div>
        
        <div className="card-actions justify-end">
          {budget.percentual >= 80 && (
            <div className="badge badge-warning">Atenção</div>
          )}
          {budget.percentual >= 100 && (
            <div className="badge badge-error">Excedido</div>
          )}
        </div>
      </div>
    </div>
  ))}
</div>
```

### 4. Goal CRUD

#### Card de Meta (DaisyUI + shadcn/ui)
```tsx
<div className="card bg-base-100 shadow-xl">
  <div className="card-body">
    <h2 className="card-title">{goal.nome}</h2>
    
    <div className="space-y-2">
      <div className="flex justify-between text-sm">
        <span>Progresso</span>
        <span className="font-semibold">{goal.percentual}%</span>
      </div>
      <Progress value={goal.percentual} className="h-2" />
      <div className="flex justify-between text-xs text-gray-500">
        <span>R$ {goal.valorAtual}</span>
        <span>R$ {goal.valorAlvo}</span>
      </div>
    </div>
    
    <div className="divider my-2"></div>
    
    <div className="flex justify-between items-center">
      <div className="text-sm">
        <p className="text-gray-500">Prazo</p>
        <p className="font-semibold">{formatDate(goal.prazo)}</p>
      </div>
      <Button
        size="sm"
        variant="outline"
        onClick={() => handleUpdateProgress(goal)}
      >
        Adicionar Progresso
      </Button>
    </div>
  </div>
</div>
```

#### Modal de Atualizar Progresso (shadcn/ui)
```tsx
<Modal open={isOpen} onOpenChange={setIsOpen}>
  <ModalContent>
    <ModalHeader>
      <ModalTitle>Adicionar Progresso</ModalTitle>
      <ModalDescription>
        Adicione um valor ao progresso da meta "{goal.nome}"
      </ModalDescription>
    </ModalHeader>
    
    <div className="space-y-4 py-4">
      <div className="space-y-2">
        <label className="text-sm font-medium">Valor a Adicionar</label>
        <Input
          type="number"
          step="0.01"
          placeholder="0.00"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
        />
      </div>
      
      <div className="alert alert-info">
        <Info className="h-4 w-4" />
        <span className="text-sm">
          Progresso atual: R$ {goal.valorAtual} / R$ {goal.valorAlvo}
        </span>
      </div>
    </div>
    
    <ModalFooter>
      <Button variant="outline" onClick={() => setIsOpen(false)}>
        Cancelar
      </Button>
      <Button onClick={handleSubmit}>
        Adicionar
      </Button>
    </ModalFooter>
  </ModalContent>
</Modal>
```

## 🎨 Padrões de Design

### Loading States (DaisyUI)
```tsx
<div className="flex justify-center items-center h-64">
  <span className="loading loading-spinner loading-lg"></span>
</div>
```

### Empty States (DaisyUI)
```tsx
<div className="hero min-h-[400px] bg-base-200">
  <div className="hero-content text-center">
    <div className="max-w-md">
      <Inbox className="mx-auto h-16 w-16 text-gray-400 mb-4" />
      <h1 className="text-2xl font-bold">Nenhum orçamento encontrado</h1>
      <p className="py-6">
        Comece criando seu primeiro orçamento para controlar seus gastos.
      </p>
      <Button onClick={handleCreate}>
        Criar Orçamento
      </Button>
    </div>
  </div>
</div>
```

### Error States (DaisyUI)
```tsx
<div className="alert alert-error shadow-lg">
  <div>
    <AlertCircle className="h-6 w-6" />
    <div>
      <h3 className="font-bold">Erro ao carregar dados</h3>
      <div className="text-xs">{error.message}</div>
    </div>
  </div>
  <button className="btn btn-sm" onClick={handleRetry}>
    Tentar Novamente
  </button>
</div>
```

### Success Toast (shadcn/ui)
```tsx
import { toast } from "@/components/ui/use-toast"

toast({
  title: "Sucesso!",
  description: "Orçamento criado com sucesso.",
  variant: "success",
})
```

## 📱 Responsividade

### Breakpoints
```css
sm: 640px   /* Mobile landscape */
md: 768px   /* Tablet */
lg: 1024px  /* Desktop */
xl: 1280px  /* Large desktop */
```

### Grid Responsivo
```tsx
<div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
  {/* Cards */}
</div>
```

### Stack em Mobile
```tsx
<div className="flex flex-col md:flex-row gap-4">
  <div className="flex-1">{/* Conteúdo */}</div>
  <div className="flex-1">{/* Conteúdo */}</div>
</div>
```

## ♿ Acessibilidade

### Sempre Incluir
- `aria-label` em botões sem texto
- `alt` em imagens
- `role` em elementos interativos
- Navegação por teclado
- Contraste adequado de cores

### Exemplo
```tsx
<button
  aria-label="Excluir orçamento"
  className="btn btn-ghost btn-sm"
  onClick={handleDelete}
>
  <Trash2 className="h-4 w-4" />
</button>
```

## 🎯 Checklist de UI/UX

Para cada nova tela/componente:

- [ ] Usa shadcn/ui para componentes interativos
- [ ] Usa DaisyUI para cards e containers
- [ ] Usa TailwindCSS para layout e spacing
- [ ] É responsivo (mobile, tablet, desktop)
- [ ] Tem loading state
- [ ] Tem empty state
- [ ] Tem error state
- [ ] É acessível (ARIA labels, keyboard navigation)
- [ ] Segue o design system (cores, tipografia, spacing)
- [ ] Tem feedback visual para ações do usuário
- [ ] Usa animações sutis (transitions)

## 📚 Recursos

- **shadcn/ui**: https://ui.shadcn.com/
- **DaisyUI**: https://daisyui.com/
- **TailwindCSS**: https://tailwindcss.com/
- **Radix UI**: https://www.radix-ui.com/
- **Lucide Icons**: https://lucide.dev/

---

**Mantenha consistência visual em toda a aplicação!** 🎨
