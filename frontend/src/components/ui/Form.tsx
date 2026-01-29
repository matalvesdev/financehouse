import React, { ReactNode } from 'react'
import { UseFormReturn, FieldValues } from 'react-hook-form'
import { cn } from '@/lib/utils'
import { getAllErrors } from '@/lib/form-utils'

interface FormProps<T extends FieldValues> extends React.FormHTMLAttributes<HTMLFormElement> {
  form: UseFormReturn<T>
  onSubmit: (data: T) => void | Promise<void>
  children: ReactNode
  showErrorSummary?: boolean
  errorSummaryTitle?: string
  className?: string
}

/**
 * Enhanced Form component that integrates with React Hook Form and Zod validation
 * Provides automatic error handling and submission management
 */
export function Form<T extends FieldValues>({
  form,
  onSubmit,
  children,
  showErrorSummary = false,
  errorSummaryTitle = 'Por favor, corrija os seguintes erros:',
  className,
  ...props
}: FormProps<T>) {
  const { handleSubmit, formState: { errors, isSubmitting } } = form
  const errorMessages = getAllErrors(errors)

  const handleFormSubmit = async (data: T) => {
    try {
      await onSubmit(data)
    } catch (error) {
      // Error handling is typically done in the onSubmit function
      console.error('Form submission error:', error)
    }
  }

  return (
    <form
      onSubmit={handleSubmit(handleFormSubmit)}
      className={cn('space-y-4', className)}
      {...props}
    >
      {showErrorSummary && errorMessages.length > 0 && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4">
          <h3 className="text-sm font-medium text-red-800 mb-2">
            {errorSummaryTitle}
          </h3>
          <ul className="text-sm text-red-700 space-y-1">
            {errorMessages.map((error, index) => (
              <li key={index} className="flex items-start">
                <span className="mr-2">•</span>
                <span>{error}</span>
              </li>
            ))}
          </ul>
        </div>
      )}
      
      <fieldset disabled={isSubmitting} className="space-y-4">
        {children}
      </fieldset>
    </form>
  )
}

interface FormFieldProps {
  children: ReactNode
  className?: string
}

/**
 * Form field wrapper component for consistent spacing and layout
 */
export function FormField({ children, className }: FormFieldProps) {
  return (
    <div className={cn('space-y-1', className)}>
      {children}
    </div>
  )
}

interface FormSectionProps {
  title?: string
  description?: string
  children: ReactNode
  className?: string
}

/**
 * Form section component for grouping related fields
 */
export function FormSection({ title, description, children, className }: FormSectionProps) {
  return (
    <div className={cn('space-y-4', className)}>
      {(title || description) && (
        <div className="space-y-1">
          {title && (
            <h3 className="text-lg font-medium text-gray-900">{title}</h3>
          )}
          {description && (
            <p className="text-sm text-gray-600">{description}</p>
          )}
        </div>
      )}
      <div className="space-y-4">
        {children}
      </div>
    </div>
  )
}

interface FormActionsProps {
  children: ReactNode
  className?: string
  align?: 'left' | 'center' | 'right'
}

/**
 * Form actions component for submit/cancel buttons
 */
export function FormActions({ children, className, align = 'right' }: FormActionsProps) {
  const alignmentClasses = {
    left: 'justify-start',
    center: 'justify-center',
    right: 'justify-end',
  }

  return (
    <div className={cn(
      'flex items-center space-x-3 pt-4 border-t border-gray-200',
      alignmentClasses[align],
      className
    )}>
      {children}
    </div>
  )
}

export default Form