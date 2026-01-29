import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import FileUpload from '../FileUpload'

describe('FileUpload Component', () => {
  const mockOnFileSelect = vi.fn()

  beforeEach(() => {
    mockOnFileSelect.mockClear()
  })

  describe('File Format Validation', () => {
    it('should accept valid Excel .xlsx files', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      expect(input).toBeTruthy()

      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(mockOnFileSelect).toHaveBeenCalledWith(file)
      expect(screen.getByText('test.xlsx')).toBeInTheDocument()
    })

    it('should accept valid Excel .xls files', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.xls', {
        type: 'application/vnd.ms-excel',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(mockOnFileSelect).toHaveBeenCalledWith(file)
    })

    it('should accept valid CSV files', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.csv', {
        type: 'text/csv',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(mockOnFileSelect).toHaveBeenCalledWith(file)
    })

    it('should reject invalid file formats', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.pdf', {
        type: 'application/pdf',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(mockOnFileSelect).not.toHaveBeenCalled()
      expect(screen.getByText(/formato de arquivo inválido/i)).toBeInTheDocument()
    })

    it('should reject files that are too large', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} maxSizeMB={1} />)

      // Create a file larger than 1MB
      const largeContent = new Array(2 * 1024 * 1024).fill('a').join('')
      const file = new File([largeContent], 'large.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(mockOnFileSelect).not.toHaveBeenCalled()
      expect(screen.getByText(/arquivo muito grande/i)).toBeInTheDocument()
    })
  })

  describe('Drag and Drop Interactions', () => {
    it('should show drag state when dragging over', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const dropZone = screen.getByText(/arraste e solte seu arquivo aqui/i).closest('div')
      expect(dropZone).toBeTruthy()

      if (dropZone) {
        fireEvent.dragOver(dropZone)
        expect(screen.getByText(/solte o arquivo aqui/i)).toBeInTheDocument()
      }
    })

    it('should remove drag state when dragging leaves', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const dropZone = screen.getByText(/arraste e solte seu arquivo aqui/i).closest('div')
      
      if (dropZone) {
        fireEvent.dragOver(dropZone)
        expect(screen.getByText(/solte o arquivo aqui/i)).toBeInTheDocument()

        fireEvent.dragLeave(dropZone)
        expect(screen.getByText(/arraste e solte seu arquivo aqui/i)).toBeInTheDocument()
      }
    })

    it('should handle file drop', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'dropped.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const dropZone = screen.getByText(/arraste e solte seu arquivo aqui/i).closest('div')
      
      if (dropZone) {
        fireEvent.drop(dropZone, {
          dataTransfer: {
            files: [file],
          },
        })

        expect(mockOnFileSelect).toHaveBeenCalledWith(file)
        expect(screen.getByText('dropped.xlsx')).toBeInTheDocument()
      }
    })
  })

  describe('File Removal', () => {
    it('should allow removing selected file', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(screen.getByText('test.xlsx')).toBeInTheDocument()

      const removeButton = screen.getByLabelText(/remover arquivo/i)
      fireEvent.click(removeButton)

      expect(screen.queryByText('test.xlsx')).not.toBeInTheDocument()
    })
  })

  describe('Visual Feedback', () => {
    it('should display loading state when uploading', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} isUploading={true} />)

      expect(screen.getByText(/processando arquivo/i)).toBeInTheDocument()
      expect(screen.getByText(/isso pode levar alguns segundos/i)).toBeInTheDocument()
    })

    it('should display file information after selection', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['a'.repeat(1024)], 'test.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(screen.getByText('test.xlsx')).toBeInTheDocument()
      expect(screen.getByText(/1\.0 KB/i)).toBeInTheDocument()
    })

    it('should show validation error with appropriate styling', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.txt', {
        type: 'text/plain',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(screen.getByText(/erro de validação/i)).toBeInTheDocument()
      expect(screen.getByText(/formato de arquivo inválido/i)).toBeInTheDocument()
    })
  })

  describe('File Size Formatting', () => {
    it('should format bytes correctly', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['a'.repeat(500)], 'small.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(screen.getByText(/500 B/i)).toBeInTheDocument()
    })

    it('should format kilobytes correctly', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['a'.repeat(2048)], 'medium.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      expect(screen.getByText(/2\.0 KB/i)).toBeInTheDocument()
    })
  })

  describe('Accessibility', () => {
    it('should have proper aria labels', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
      if (input) {
        fireEvent.change(input, { target: { files: [file] } })
      }

      const removeButton = screen.getByLabelText(/remover arquivo/i)
      expect(removeButton).toBeInTheDocument()
    })
  })

  describe('Input Reset', () => {
    it('should allow selecting the same file twice', () => {
      render(<FileUpload onFileSelect={mockOnFileSelect} />)

      const file = new File(['content'], 'test.xlsx', {
        type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      })

      const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input') as HTMLInputElement
      
      if (input) {
        // First selection
        fireEvent.change(input, { target: { files: [file] } })
        expect(mockOnFileSelect).toHaveBeenCalledTimes(1)

        // Remove file
        const removeButton = screen.getByLabelText(/remover arquivo/i)
        fireEvent.click(removeButton)

        // Second selection of same file
        fireEvent.change(input, { target: { files: [file] } })
        expect(mockOnFileSelect).toHaveBeenCalledTimes(2)
      }
    })
  })
})
