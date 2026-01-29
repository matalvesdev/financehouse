import { useForm, UseFormProps, UseFormReturn, FieldValues } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'

/**
 * Custom hook that integrates React Hook Form with Zod validation
 * Provides a simplified API for form creation with automatic Zod resolver setup
 */
export function useValidatedForm<T extends z.ZodType<any, any, any>>(
  schema: T,
  options?: Omit<UseFormProps<z.infer<T>>, 'resolver'>
): UseFormReturn<z.infer<T>> {
  return useForm<z.infer<T>>({
    resolver: zodResolver(schema),
    mode: 'onChange', // Enable real-time validation
    ...options,
  })
}

/**
 * Utility function to extract field error messages from React Hook Form errors
 * Useful for displaying validation errors in custom components
 */
export function getFieldError(
  errors: Record<string, any>,
  fieldName: string
): string | undefined {
  const error = errors[fieldName]
  return error?.message
}

/**
 * Utility function to check if a field has an error
 * Useful for conditional styling based on validation state
 */
export function hasFieldError(
  errors: Record<string, any>,
  fieldName: string
): boolean {
  return !!errors[fieldName]
}

/**
 * Utility function to get all error messages from a form
 * Useful for displaying a summary of all validation errors
 */
export function getAllErrors(errors: Record<string, any>): string[] {
  const errorMessages: string[] = []
  
  const extractErrors = (obj: any, prefix = '') => {
    Object.keys(obj).forEach(key => {
      const fullKey = prefix ? `${prefix}.${key}` : key
      const error = obj[key]
      
      if (error?.message) {
        errorMessages.push(error.message)
      } else if (typeof error === 'object' && error !== null) {
        extractErrors(error, fullKey)
      }
    })
  }
  
  extractErrors(errors)
  return errorMessages
}

/**
 * Utility function to reset form with default values based on schema
 * Useful for resetting forms to a clean state
 */
export function getSchemaDefaults<T extends z.ZodType>(schema: T): Partial<z.infer<T>> {
  try {
    // Try to parse an empty object to get default values
    const result = schema.safeParse({})
    if (result.success) {
      return result.data
    }
    
    // If that fails, return an empty object
    return {}
  } catch {
    return {}
  }
}

/**
 * Utility function to validate data against a schema without using React Hook Form
 * Useful for one-off validations or server-side validation
 */
export function validateWithSchema<T extends z.ZodType>(
  schema: T,
  data: unknown
): { success: true; data: z.infer<T> } | { success: false; errors: string[] } {
  const result = schema.safeParse(data)
  
  if (result.success) {
    return { success: true, data: result.data }
  }
  
  const errors = result.error.issues.map(issue => issue.message)
  return { success: false, errors }
}

/**
 * Utility function to create form field props for easier integration
 * Returns props that can be spread directly onto form inputs
 */
export function createFieldProps<T extends FieldValues>(
  register: UseFormReturn<T>['register'],
  errors: UseFormReturn<T>['formState']['errors'],
  fieldName: keyof T,
  options?: Parameters<UseFormReturn<T>['register']>[1]
) {
  return {
    ...register(fieldName as string, options),
    error: getFieldError(errors, fieldName as string),
    hasError: hasFieldError(errors, fieldName as string),
  }
}

/**
 * Type helper to extract form data type from a Zod schema
 */
export type FormDataFromSchema<T extends z.ZodType> = z.infer<T>

/**
 * Type helper to create form props interface
 */
export interface FormProps<T extends z.ZodType> {
  onSubmit: (data: z.infer<T>) => void | Promise<void>
  defaultValues?: Partial<z.infer<T>>
  isLoading?: boolean
  disabled?: boolean
}

/**
 * Utility function to transform Zod enum to select options
 * Useful for creating dropdown options from Zod enums
 */
export function enumToSelectOptions<T extends Record<string, string>>(
  enumObject: T,
  labels?: Record<keyof T, string>
): Array<{ value: keyof T; label: string }> {
  return Object.keys(enumObject).map(key => ({
    value: key as keyof T,
    label: labels?.[key as keyof T] || enumObject[key as keyof T],
  }))
}

/**
 * Utility function to create a debounced validation function
 * Useful for expensive validations that shouldn't run on every keystroke
 */
export function createDebouncedValidator<T extends z.ZodType>(
  schema: T,
  delay: number = 300
) {
  let timeoutId: NodeJS.Timeout | null = null
  
  return (
    data: unknown,
    callback: (result: { success: boolean; errors?: string[] }) => void
  ) => {
    if (timeoutId) {
      clearTimeout(timeoutId)
    }
    
    timeoutId = setTimeout(() => {
      const validation = validateWithSchema(schema, data)
      callback({
        success: validation.success,
        errors: validation.success ? undefined : validation.errors,
      })
    }, delay)
  }
}

/**
 * Utility function to merge multiple Zod schemas
 * Useful for creating complex forms with multiple validation schemas
 */
export function mergeSchemas<T extends z.ZodType, U extends z.ZodType>(
  schema1: T,
  schema2: U
): z.ZodIntersection<T, U> {
  return z.intersection(schema1, schema2)
}

/**
 * Utility function to create conditional validation
 * Useful for fields that have different validation rules based on other field values
 */
export function createConditionalSchema<T extends z.ZodRawShape>(
  baseSchema: z.ZodObject<T>,
  condition: (data: z.infer<z.ZodObject<T>>) => boolean,
  conditionalFields: z.ZodRawShape
): z.ZodEffects<z.ZodObject<T & typeof conditionalFields>> {
  const mergedSchema = baseSchema.extend(conditionalFields)
  
  return mergedSchema.refine(
    (data) => {
      if (condition(data)) {
        // Validate conditional fields
        const conditionalSchema = z.object(conditionalFields)
        const result = conditionalSchema.safeParse(data)
        return result.success
      }
      return true
    },
    {
      message: 'Conditional validation failed',
    }
  )
}