/**
 * Migration Example: Old vs New Form Implementation
 * 
 * This file demonstrates how to migrate from the traditional React Hook Form + Zod
 * implementation to the enhanced integration provided by the form utilities.
 */

import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useValidatedForm } from '@/lib/form-utils'
import { loginSchema, type LoginFormData } from '@/lib/schemas'
import { Form, FormInput, FormActions, Button, Input } from '@/components/ui'

// ============================================
// OLD IMPLEMENTATION (Traditional approach)
// ============================================

export function OldLoginForm() {
  // Manual setup with zodResolver
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
  })

  const onSubmit = async (data: LoginFormData) => {
    // Handle submission
    console.log(data)
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
      {/* Manual error handling */}
      <Input
        label="Email"
        type="email"
        error={errors.email?.message}
        {...register('email')}
      />
      
      <Input
        label="Senha"
        type="password"
        error={errors.password?.message}
        {...register('password')}
      />
      
      <Button
        type="submit"
        isLoading={isSubmitting}
        className="w-full"
      >
        Entrar
      </Button>
    </form>
  )
}

// ============================================
// NEW IMPLEMENTATION (Enhanced approach)
// ============================================

export function NewLoginForm() {
  // Simplified setup with enhanced hook
  const form = useValidatedForm(loginSchema)

  const onSubmit = async (data: LoginFormData) => {
    // Handle submission
    console.log(data)
  }

  return (
    <Form form={form} onSubmit={onSubmit} showErrorSummary>
      {/* Automatic integration with form state */}
      <FormInput
        form={form}
        name="email"
        label="Email"
        type="email"
        helperText="Digite seu email de acesso"
      />
      
      <FormInput
        form={form}
        name="password"
        label="Senha"
        type="password"
        helperText="Digite sua senha"
      />
      
      <FormActions>
        <Button type="submit" variant="primary" className="w-full">
          Entrar
        </Button>
      </FormActions>
    </Form>
  )
}

// ============================================
// MIGRATION BENEFITS
// ============================================

/**
 * Benefits of the new approach:
 * 
 * 1. LESS BOILERPLATE
 *    - No need to manually setup zodResolver
 *    - Automatic form state management
 *    - Built-in error handling
 * 
 * 2. BETTER TYPE SAFETY
 *    - Automatic type inference from schema
 *    - Compile-time field name validation
 *    - Better IDE support
 * 
 * 3. CONSISTENT UX
 *    - Standardized error display
 *    - Consistent form layout
 *    - Built-in loading states
 * 
 * 4. ENHANCED FEATURES
 *    - Error summary display
 *    - Helper text support
 *    - Form sections and actions
 *    - Automatic field registration
 * 
 * 5. EASIER TESTING
 *    - Simplified test setup
 *    - Better error message testing
 *    - Consistent component structure
 */

// ============================================
// MIGRATION STEPS
// ============================================

/**
 * How to migrate existing forms:
 * 
 * 1. Replace useForm with useValidatedForm:
 *    OLD: const form = useForm({ resolver: zodResolver(schema) })
 *    NEW: const form = useValidatedForm(schema)
 * 
 * 2. Wrap form with Form component:
 *    OLD: <form onSubmit={handleSubmit(onSubmit)}>
 *    NEW: <Form form={form} onSubmit={onSubmit}>
 * 
 * 3. Replace Input components with FormInput:
 *    OLD: <Input {...register('field')} error={errors.field?.message} />
 *    NEW: <FormInput form={form} name="field" />
 * 
 * 4. Use FormActions for buttons:
 *    OLD: <Button type="submit">Submit</Button>
 *    NEW: <FormActions><Button type="submit">Submit</Button></FormActions>
 * 
 * 5. Add error summary if needed:
 *    NEW: <Form form={form} onSubmit={onSubmit} showErrorSummary>
 */

// ============================================
// ADVANCED EXAMPLE
// ============================================

export function AdvancedFormExample() {
  const form = useValidatedForm(loginSchema, {
    defaultValues: {
      email: '',
      password: '',
    },
    mode: 'onChange', // Real-time validation
  })

  const onSubmit = async (data: LoginFormData) => {
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1000))
      console.log('Success:', data)
    } catch (error) {
      console.error('Error:', error)
    }
  }

  return (
    <Form 
      form={form} 
      onSubmit={onSubmit} 
      showErrorSummary
      className="max-w-md mx-auto"
    >
      <div className="text-center mb-6">
        <h2 className="text-2xl font-bold text-gray-900">Login</h2>
        <p className="text-gray-600">Entre na sua conta</p>
      </div>

      <FormInput
        form={form}
        name="email"
        label="Email"
        type="email"
        placeholder="seu@email.com"
        helperText="Digite o email cadastrado na sua conta"
      />
      
      <FormInput
        form={form}
        name="password"
        label="Senha"
        type="password"
        placeholder="••••••••"
        helperText="Mínimo 8 caracteres"
      />
      
      <FormActions align="center">
        <Button type="submit" variant="primary" className="w-full">
          Entrar
        </Button>
      </FormActions>
    </Form>
  )
}