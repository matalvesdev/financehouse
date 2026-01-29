# Zod + React Hook Form Integration Guide

Este guia explica como usar a integração aprimorada entre Zod e React Hook Form no projeto de gestão financeira doméstica.

## Visão Geral

A integração fornece:
- ✅ Validação automática com Zod schemas
- ✅ Componentes de formulário tipados
- ✅ Utilitários para simplificar o desenvolvimento
- ✅ Tratamento consistente de erros
- ✅ Integração perfeita com TypeScript

## Componentes Principais

### 1. Hook `useValidatedForm`

Hook personalizado que simplifica a criação de formulários com validação Zod.

```typescript
import { useValidatedForm } from '@/lib/form-utils'
import { loginSchema } from '@/lib/schemas'

function LoginForm() {
  const form = useValidatedForm(loginSchema, {
    defaultValues: {
      email: '',
      password: '',
    }
  })

  const onSubmit = (data: LoginFormData) => {
    // Dados já validados pelo Zod
    console.log(data)
  }

  return (
    <Form form={form} onSubmit={onSubmit}>
      {/* Campos do formulário */}
    </Form>
  )
}
```

### 2. Componente `Form`

Componente wrapper que gerencia submissão e exibição de erros.

```typescript
import { Form, FormSection, FormActions } from '@/components/ui'

<Form
  form={form}
  onSubmit={handleSubmit}
  showErrorSummary={true} // Mostra resumo de erros
>
  <FormSection title="Dados Pessoais">
    {/* Campos */}
  </FormSection>
  
  <FormActions>
    <Button type="submit">Salvar</Button>
  </FormActions>
</Form>
```

### 3. Componentes `FormInput` e `FormSelect`

Componentes que se integram automaticamente com React Hook Form.

```typescript
import { FormInput, FormSelect } from '@/components/ui'

// Input com integração automática
<FormInput
  form={form}
  name="email"
  label="Email"
  type="email"
  helperText="Digite seu email"
/>

// Select com opções
<FormSelect
  form={form}
  name="categoria"
  label="Categoria"
  options={categoriaOptions}
  placeholder="Selecione uma categoria"
/>
```

## Utilitários Disponíveis

### 1. Manipulação de Erros

```typescript
import { getFieldError, hasFieldError, getAllErrors } from '@/lib/form-utils'

// Obter erro de um campo específico
const emailError = getFieldError(errors, 'email')

// Verificar se campo tem erro
const hasError = hasFieldError(errors, 'email')

// Obter todos os erros do formulário
const allErrors = getAllErrors(errors)
```

### 2. Conversão de Enums

```typescript
import { enumToSelectOptions } from '@/lib/form-utils'
import { categoriaEnum, categoriaLabels } from '@/lib/schemas'

// Converter enum Zod para opções de select
const options = enumToSelectOptions(categoriaEnum, categoriaLabels)
// Resultado: [{ value: 'ALIMENTACAO', label: 'Alimentação' }, ...]
```

### 3. Validação Manual

```typescript
import { validateWithSchema } from '@/lib/form-utils'
import { transactionSchema } from '@/lib/schemas'

const result = validateWithSchema(transactionSchema, formData)

if (result.success) {
  console.log('Dados válidos:', result.data)
} else {
  console.log('Erros:', result.errors)
}
```

### 4. Validação com Debounce

```typescript
import { createDebouncedValidator } from '@/lib/form-utils'

const validator = createDebouncedValidator(emailSchema, 500)

validator(emailValue, (result) => {
  if (!result.success) {
    setEmailError(result.errors?.[0])
  }
})
```

## Schemas Disponíveis

O projeto inclui schemas Zod para todas as entidades:

```typescript
import {
  // Autenticação
  loginSchema,
  registerSchema,
  
  // Transações
  transactionSchema,
  transactionFilterSchema,
  
  // Orçamentos
  budgetSchema,
  budgetUpdateSchema,
  
  // Metas
  goalSchema,
  goalProgressSchema,
  
  // Importação
  importFileSchema,
  importConfirmSchema,
  
  // Confirmação
  confirmActionSchema,
} from '@/lib/schemas'
```

## Exemplo Completo

```typescript
import { useValidatedForm, enumToSelectOptions } from '@/lib/form-utils'
import { transactionSchema, categoriaLabels } from '@/lib/schemas'
import { Form, FormInput, FormSelect, FormActions, Button } from '@/components/ui'

interface TransactionFormProps {
  onSubmit: (data: TransactionFormData) => Promise<void>
  initialData?: Partial<TransactionFormData>
}

export function TransactionForm({ onSubmit, initialData }: TransactionFormProps) {
  const form = useValidatedForm(transactionSchema, {
    defaultValues: {
      data: format(new Date(), 'yyyy-MM-dd'),
      ...initialData,
    }
  })

  const categoriaOptions = enumToSelectOptions(categoriaEnum, categoriaLabels)

  return (
    <Form form={form} onSubmit={onSubmit} showErrorSummary>
      <FormInput
        form={form}
        name="valor"
        label="Valor"
        type="number"
        step="0.01"
        registerOptions={{ valueAsNumber: true }}
      />
      
      <FormInput
        form={form}
        name="descricao"
        label="Descrição"
        placeholder="Descreva a transação"
      />
      
      <FormSelect
        form={form}
        name="categoria"
        label="Categoria"
        options={categoriaOptions}
        placeholder="Selecione uma categoria"
      />
      
      <FormActions>
        <Button type="submit" variant="primary">
          Salvar
        </Button>
      </FormActions>
    </Form>
  )
}
```

## Padrões de Validação

### 1. Validação Condicional

```typescript
const conditionalSchema = z.object({
  type: z.enum(['individual', 'company']),
  name: z.string(),
  document: z.string(),
}).refine((data) => {
  if (data.type === 'individual') {
    return data.document.length === 11 // CPF
  } else {
    return data.document.length === 14 // CNPJ
  }
}, {
  message: 'Documento inválido para o tipo selecionado',
  path: ['document']
})
```

### 2. Validação Assíncrona

```typescript
const asyncSchema = z.object({
  email: z.string().email(),
}).refine(async (data) => {
  const exists = await checkEmailExists(data.email)
  return !exists
}, {
  message: 'Email já está em uso',
  path: ['email']
})
```

### 3. Transformação de Dados

```typescript
const transformSchema = z.object({
  price: z.string().transform((val) => parseFloat(val)),
  date: z.string().transform((val) => new Date(val)),
})
```

## Testes

### Testando Schemas

```typescript
import { describe, it, expect } from 'vitest'
import { transactionSchema } from '@/lib/schemas'

describe('Transaction Schema', () => {
  it('deve validar transação válida', () => {
    const validData = {
      valor: 100.50,
      descricao: 'Compra no mercado',
      categoria: 'ALIMENTACAO',
      tipo: 'DESPESA',
      data: '2024-01-15'
    }
    
    const result = transactionSchema.safeParse(validData)
    expect(result.success).toBe(true)
  })
})
```

### Testando Componentes de Formulário

```typescript
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { TransactionForm } from './TransactionForm'

describe('TransactionForm', () => {
  it('deve exibir erros de validação', async () => {
    const onSubmit = vi.fn()
    render(<TransactionForm onSubmit={onSubmit} />)
    
    // Submeter formulário vazio
    fireEvent.click(screen.getByText('Salvar'))
    
    await waitFor(() => {
      expect(screen.getByText('Valor é obrigatório')).toBeInTheDocument()
    })
  })
})
```

## Melhores Práticas

### 1. Organização de Schemas

- Mantenha schemas em arquivos separados por domínio
- Use enums para valores fixos
- Documente validações complexas

### 2. Reutilização

- Crie schemas base e estenda quando necessário
- Use utilitários para conversões comuns
- Compartilhe validações entre frontend e backend

### 3. Performance

- Use validação com debounce para campos custosos
- Evite validações desnecessárias em tempo real
- Cache resultados de validações assíncronas

### 4. UX

- Forneça mensagens de erro claras
- Use helper text para orientar o usuário
- Implemente validação progressiva (conforme o usuário digita)

## Troubleshooting

### Problema: Validação não funciona

**Solução**: Verifique se o schema está sendo passado corretamente para `useValidatedForm`.

### Problema: Tipos TypeScript incorretos

**Solução**: Use `z.infer<typeof schema>` para inferir tipos automaticamente.

### Problema: Performance lenta

**Solução**: Use validação com debounce ou mova validações custosas para onSubmit.

### Problema: Erros não aparecem

**Solução**: Verifique se `showError={true}` está definido nos componentes de input.