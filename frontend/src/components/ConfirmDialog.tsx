import { useEffect } from 'react'
import { useConfirmStore } from '@/stores/confirmStore'
import { AlertTriangle, Info, AlertCircle, X } from 'lucide-react'
import { clsx } from 'clsx'

export default function ConfirmDialog() {
  const { isOpen, options, timeRemaining, closeConfirm, confirm, setTimeRemaining } = useConfirmStore()

  // Timer para timeout automático
  useEffect(() => {
    if (!isOpen || timeRemaining === null) return

    const interval = setInterval(() => {
      setTimeRemaining(timeRemaining - 1)
    }, 1000)

    return () => clearInterval(interval)
  }, [isOpen, timeRemaining, setTimeRemaining])

  if (!isOpen || !options) return null

  const variantStyles = {
    danger: {
      icon: AlertTriangle,
      iconColor: 'text-red-600',
      iconBg: 'bg-red-100',
      confirmBg: 'bg-red-600 hover:bg-red-700',
    },
    warning: {
      icon: AlertCircle,
      iconColor: 'text-yellow-600',
      iconBg: 'bg-yellow-100',
      confirmBg: 'bg-yellow-600 hover:bg-yellow-700',
    },
    info: {
      icon: Info,
      iconColor: 'text-blue-600',
      iconBg: 'bg-blue-100',
      confirmBg: 'bg-blue-600 hover:bg-blue-700',
    },
  }

  const variant = options.variant || 'danger'
  const styles = variantStyles[variant]
  const Icon = styles.icon

  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60)
    const secs = seconds % 60
    return `${mins}:${secs.toString().padStart(2, '0')}`
  }

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto">
      {/* Overlay */}
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
        onClick={closeConfirm}
      />

      {/* Dialog */}
      <div className="flex min-h-full items-center justify-center p-4">
        <div className="relative bg-white rounded-lg shadow-xl max-w-md w-full p-6 transform transition-all">
          {/* Close button */}
          <button
            onClick={closeConfirm}
            className="absolute top-4 right-4 text-gray-400 hover:text-gray-500"
          >
            <X className="h-5 w-5" />
          </button>

          {/* Content */}
          <div className="flex items-start space-x-4">
            <div className={clsx('p-3 rounded-full', styles.iconBg)}>
              <Icon className={clsx('h-6 w-6', styles.iconColor)} />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-gray-900">
                {options.title}
              </h3>
              <p className="mt-2 text-sm text-gray-600">
                {options.message}
              </p>
              {/* Impact section (Requirement 9.3) */}
              {options.impact && (
                <div className="mt-3 p-3 bg-gray-50 rounded-md border border-gray-200">
                  <p className="text-xs font-semibold text-gray-700 uppercase tracking-wide mb-1">
                    Impacto da Ação
                  </p>
                  <p className="text-sm text-gray-800">
                    {options.impact}
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* Timer */}
          {timeRemaining !== null && (
            <div className="mt-4 text-center">
              <p className="text-xs text-gray-500">
                Esta confirmação expira em{' '}
                <span className="font-mono font-semibold">
                  {formatTime(timeRemaining)}
                </span>
              </p>
              <div className="mt-2 h-1 bg-gray-200 rounded-full overflow-hidden">
                <div
                  className="h-full bg-gray-400 transition-all duration-1000"
                  style={{
                    width: `${((options.timeoutSeconds || 300) - timeRemaining) / (options.timeoutSeconds || 300) * 100}%`,
                  }}
                />
              </div>
            </div>
          )}

          {/* Actions */}
          <div className="mt-6 flex justify-end space-x-3">
            <button
              onClick={closeConfirm}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
            >
              {options.cancelText || 'Cancelar'}
            </button>
            <button
              onClick={confirm}
              className={clsx(
                'px-4 py-2 text-sm font-medium text-white rounded-md focus:outline-none focus:ring-2 focus:ring-offset-2',
                styles.confirmBg
              )}
            >
              {options.confirmText || 'Confirmar'}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
