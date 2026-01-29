import { useCallback, useState } from 'react'
import { Upload, FileSpreadsheet, X, AlertCircle } from 'lucide-react'
import { cn } from '@/lib/utils'

interface FileUploadProps {
  onFileSelect: (file: File) => void
  isUploading?: boolean
  accept?: string
  maxSizeMB?: number
  className?: string
}

interface FileValidationError {
  type: 'format' | 'size' | 'general'
  message: string
}

const ACCEPTED_FILE_TYPES = [
  'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', // .xlsx
  'application/vnd.ms-excel', // .xls
  'text/csv', // .csv
]

export default function FileUpload({
  onFileSelect,
  isUploading = false,
  accept = '.xlsx,.xls,.csv',
  maxSizeMB = 10,
  className,
}: FileUploadProps) {
  const [isDragging, setIsDragging] = useState(false)
  const [selectedFile, setSelectedFile] = useState<File | null>(null)
  const [validationError, setValidationError] = useState<FileValidationError | null>(null)

  const handleFile = useCallback(
    (file: File) => {
      setValidationError(null)

      // Validate file format
      if (!ACCEPTED_FILE_TYPES.includes(file.type)) {
        const error: FileValidationError = {
          type: 'format',
          message: 'Formato de arquivo inválido. Use Excel (.xlsx, .xls) ou CSV.',
        }
        setValidationError(error)
        setSelectedFile(null)
        return
      }

      // Validate file size
      const maxSizeBytes = maxSizeMB * 1024 * 1024
      if (file.size > maxSizeBytes) {
        const error: FileValidationError = {
          type: 'size',
          message: `Arquivo muito grande. Tamanho máximo: ${maxSizeMB}MB.`,
        }
        setValidationError(error)
        setSelectedFile(null)
        return
      }

      setSelectedFile(file)
      onFileSelect(file)
    },
    [onFileSelect, maxSizeMB]
  )

  const handleDragOver = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(true)
  }, [])

  const handleDragLeave = useCallback((e: React.DragEvent) => {
    e.preventDefault()
    e.stopPropagation()
    setIsDragging(false)
  }, [])

  const handleDrop = useCallback(
    (e: React.DragEvent) => {
      e.preventDefault()
      e.stopPropagation()
      setIsDragging(false)

      const file = e.dataTransfer.files[0]
      if (file) {
        handleFile(file)
      }
    },
    [handleFile]
  )

  const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (file) {
      handleFile(file)
    }
    // Reset input value to allow selecting the same file again
    e.target.value = ''
  }

  const handleRemoveFile = () => {
    setSelectedFile(null)
    setValidationError(null)
  }

  const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
  }

  const getFileIcon = (file: File) => {
    if (file.type.includes('csv')) {
      return <FileSpreadsheet className="h-8 w-8 text-green-500" />
    }
    return <FileSpreadsheet className="h-8 w-8 text-blue-500" />
  }

  return (
    <div className={cn('space-y-4', className)}>
      {/* Upload Area */}
      <div
        className={cn(
          'border-2 border-dashed rounded-lg p-12 text-center transition-all duration-200',
          isDragging && 'border-blue-500 bg-blue-50 scale-[1.02]',
          !isDragging && !validationError && 'border-gray-300 hover:border-gray-400',
          validationError && 'border-red-300 bg-red-50',
          isUploading && 'opacity-50 cursor-not-allowed'
        )}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        {isUploading ? (
          <div className="space-y-4">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto" />
            <p className="text-gray-600 font-medium">Processando arquivo...</p>
            <p className="text-sm text-gray-500">Isso pode levar alguns segundos</p>
          </div>
        ) : (
          <>
            <FileSpreadsheet
              className={cn(
                'h-12 w-12 mx-auto mb-4',
                isDragging ? 'text-blue-500' : 'text-gray-400'
              )}
            />
            <p className="text-lg font-medium text-gray-900 mb-2">
              {isDragging ? 'Solte o arquivo aqui' : 'Arraste e solte seu arquivo aqui'}
            </p>
            <p className="text-gray-500 mb-4">ou</p>
            <label className="cursor-pointer">
              <span className="inline-flex items-center px-4 py-2 bg-blue-600 text-white font-medium rounded-md hover:bg-blue-700 transition-colors">
                <Upload className="h-4 w-4 mr-2" />
                Selecionar arquivo
              </span>
              <input
                type="file"
                className="hidden"
                accept={accept}
                onChange={handleFileSelect}
                disabled={isUploading}
              />
            </label>
            <p className="text-sm text-gray-500 mt-4">
              Formatos suportados: Excel (.xlsx, .xls) e CSV
            </p>
            <p className="text-xs text-gray-400 mt-1">Tamanho máximo: {maxSizeMB}MB</p>
          </>
        )}
      </div>

      {/* Validation Error */}
      {validationError && (
        <div className="flex items-start space-x-3 p-4 bg-red-50 border border-red-200 rounded-lg">
          <AlertCircle className="h-5 w-5 text-red-600 flex-shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="text-sm font-medium text-red-800">Erro de validação</p>
            <p className="text-sm text-red-700 mt-1">{validationError.message}</p>
          </div>
        </div>
      )}

      {/* Selected File Info */}
      {selectedFile && !validationError && !isUploading && (
        <div className="flex items-center justify-between p-4 bg-gray-50 border border-gray-200 rounded-lg">
          <div className="flex items-center space-x-3">
            {getFileIcon(selectedFile)}
            <div>
              <p className="text-sm font-medium text-gray-900">{selectedFile.name}</p>
              <p className="text-xs text-gray-500">
                {formatFileSize(selectedFile.size)} • {selectedFile.type.split('/').pop()?.toUpperCase()}
              </p>
            </div>
          </div>
          <button
            onClick={handleRemoveFile}
            className="p-1 hover:bg-gray-200 rounded transition-colors"
            aria-label="Remover arquivo"
          >
            <X className="h-5 w-5 text-gray-500" />
          </button>
        </div>
      )}
    </div>
  )
}
