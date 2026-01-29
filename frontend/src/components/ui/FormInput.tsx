import { forwardRef, InputHTMLAttributes } from 'react'
import { UseFormReturn, FieldValues, Path } from 'react-hook-form'
import { cn } from '@/lib/utils'
import { getFieldError, hasFieldError } from '@/lib/form-utils'

interface FormInputProps<T extends FieldValues> extends Omit<InputHTMLAttributes<HTMLInputElement>, 'name'> {
  form: UseFormReturn<T>
  name: Path<T>
  label?: string
  helperText?: string
  showError?: boolean
  registerOptions?: Parameters<UseFormReturn<T>['register']>[1]
}

/**
 * Enhanced Input component that integrates directly with React Hook Form
 * Automatically handles registration, validation, and error display
 */
const FormInput = forwardRef<HTMLInputElement, FormInputProps<any>>(
  ({ 
    form, 
    name, 
    label, 
    helperText, 
    showError = true, 
    registerOptions,
    className, 
    id, 
    ...props 
  }, ref) => {
    const { register, formState: { errors } } = form
    const fieldError = getFieldError(errors, name)
    const hasError = hasFieldError(errors, name)
    const inputId = id || name

    const registration = register(name, registerOptions)

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={inputId}
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            {label}
          </label>
        )}
        <input
          {...registration}
          ref={(e) => {
            registration.ref(e)
            if (ref) {
              if (typeof ref === 'function') {
                ref(e)
              } else {
                ref.current = e
              }
            }
          }}
          id={inputId}
          className={cn(
            'appearance-none relative block w-full px-3 py-2 border rounded-md focus:outline-none focus:z-10 sm:text-sm transition-colors',
            hasError
              ? 'border-red-300 text-red-900 placeholder-red-300 focus:ring-red-500 focus:border-red-500'
              : 'border-gray-300 placeholder-gray-500 text-gray-900 focus:ring-blue-500 focus:border-blue-500',
            className
          )}
          {...props}
        />
        {showError && fieldError && (
          <p className="mt-1 text-sm text-red-600">{fieldError}</p>
        )}
        {helperText && !fieldError && (
          <p className="mt-1 text-sm text-gray-500">{helperText}</p>
        )}
      </div>
    )
  }
)

FormInput.displayName = 'FormInput'

export default FormInput