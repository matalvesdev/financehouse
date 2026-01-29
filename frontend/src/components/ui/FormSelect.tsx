import { forwardRef, SelectHTMLAttributes } from 'react'
import { UseFormReturn, FieldValues, Path } from 'react-hook-form'
import { cn } from '@/lib/utils'
import { getFieldError, hasFieldError } from '@/lib/form-utils'

interface SelectOption {
  value: string
  label: string
}

interface FormSelectProps<T extends FieldValues> extends Omit<SelectHTMLAttributes<HTMLSelectElement>, 'name'> {
  form: UseFormReturn<T>
  name: Path<T>
  label?: string
  helperText?: string
  showError?: boolean
  options: SelectOption[]
  placeholder?: string
  registerOptions?: Parameters<UseFormReturn<T>['register']>[1]
}

/**
 * Enhanced Select component that integrates directly with React Hook Form
 * Automatically handles registration, validation, and error display
 */
const FormSelect = forwardRef<HTMLSelectElement, FormSelectProps<any>>(
  ({ 
    form, 
    name, 
    label, 
    helperText, 
    showError = true, 
    options,
    placeholder,
    registerOptions,
    className, 
    id, 
    ...props 
  }, ref) => {
    const { register, formState: { errors } } = form
    const fieldError = getFieldError(errors, name)
    const hasError = hasFieldError(errors, name)
    const selectId = id || name

    const registration = register(name, registerOptions)

    return (
      <div className="w-full">
        {label && (
          <label
            htmlFor={selectId}
            className="block text-sm font-medium text-gray-700 mb-1"
          >
            {label}
          </label>
        )}
        <select
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
          id={selectId}
          className={cn(
            'appearance-none relative block w-full px-3 py-2 border rounded-md focus:outline-none focus:z-10 sm:text-sm transition-colors bg-white',
            hasError
              ? 'border-red-300 text-red-900 focus:ring-red-500 focus:border-red-500'
              : 'border-gray-300 text-gray-900 focus:ring-blue-500 focus:border-blue-500',
            className
          )}
          {...props}
        >
          {placeholder && (
            <option value="">{placeholder}</option>
          )}
          {options.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
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

FormSelect.displayName = 'FormSelect'

export default FormSelect