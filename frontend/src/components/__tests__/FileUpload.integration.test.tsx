import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import FileUpload from '../FileUpload'

describe('FileUpload Integration Tests', () => {
  it('should complete full upload workflow', async () => {
    const mockOnFileSelect = vi.fn()
    const { rerender } = render(<FileUpload onFileSelect={mockOnFileSelect} />)

    // Step 1: User selects a valid file
    const file = new File(['content'], 'transactions.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })

    const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
    if (input) {
      fireEvent.change(input, { target: { files: [file] } })
    }

    // Verify callback was called
    expect(mockOnFileSelect).toHaveBeenCalledWith(file)

    // Verify file info is displayed
    expect(screen.getByText('transactions.xlsx')).toBeInTheDocument()

    // Step 2: Simulate upload in progress
    rerender(<FileUpload onFileSelect={mockOnFileSelect} isUploading={true} />)
    expect(screen.getByText(/processando arquivo/i)).toBeInTheDocument()

    // Step 3: Upload completes
    rerender(<FileUpload onFileSelect={mockOnFileSelect} isUploading={false} />)
    expect(screen.queryByText(/processando arquivo/i)).not.toBeInTheDocument()
  })

  it('should handle error recovery workflow', () => {
    const mockOnFileSelect = vi.fn()
    render(<FileUpload onFileSelect={mockOnFileSelect} />)

    // Step 1: User tries to upload invalid file
    const invalidFile = new File(['content'], 'document.pdf', {
      type: 'application/pdf',
    })

    const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
    if (input) {
      fireEvent.change(input, { target: { files: [invalidFile] } })
    }

    // Verify error is shown
    expect(screen.getByText(/formato de arquivo inválido/i)).toBeInTheDocument()
    expect(mockOnFileSelect).not.toHaveBeenCalled()

    // Step 2: User selects valid file
    const validFile = new File(['content'], 'transactions.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })

    if (input) {
      fireEvent.change(input, { target: { files: [validFile] } })
    }

    // Verify error is cleared and file is accepted
    expect(screen.queryByText(/formato de arquivo inválido/i)).not.toBeInTheDocument()
    expect(mockOnFileSelect).toHaveBeenCalledWith(validFile)
    expect(screen.getByText('transactions.xlsx')).toBeInTheDocument()
  })

  it('should handle drag and drop workflow', () => {
    const mockOnFileSelect = vi.fn()
    render(<FileUpload onFileSelect={mockOnFileSelect} />)

    const dropZone = screen.getByText(/arraste e solte seu arquivo aqui/i).closest('div')
    expect(dropZone).toBeTruthy()

    if (dropZone) {
      // Step 1: User drags file over drop zone
      fireEvent.dragOver(dropZone)
      expect(screen.getByText(/solte o arquivo aqui/i)).toBeInTheDocument()

      // Step 2: User drops the file
      const file = new File(['content'], 'data.csv', {
        type: 'text/csv',
      })

      fireEvent.drop(dropZone, {
        dataTransfer: {
          files: [file],
        },
      })

      // Verify file was processed
      expect(mockOnFileSelect).toHaveBeenCalledWith(file)
      expect(screen.getByText('data.csv')).toBeInTheDocument()
      expect(screen.queryByText(/solte o arquivo aqui/i)).not.toBeInTheDocument()
    }
  })

  it('should handle file removal and reselection workflow', () => {
    const mockOnFileSelect = vi.fn()
    render(<FileUpload onFileSelect={mockOnFileSelect} />)

    // Step 1: Select first file
    const file1 = new File(['content1'], 'file1.xlsx', {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    })

    const input = screen.getByLabelText(/selecionar arquivo/i).parentElement?.querySelector('input')
    if (input) {
      fireEvent.change(input, { target: { files: [file1] } })
    }

    expect(screen.getByText('file1.xlsx')).toBeInTheDocument()
    expect(mockOnFileSelect).toHaveBeenCalledTimes(1)

    // Step 2: Remove file
    const removeButton = screen.getByLabelText(/remover arquivo/i)
    fireEvent.click(removeButton)

    expect(screen.queryByText('file1.xlsx')).not.toBeInTheDocument()

    // Step 3: Select different file
    const file2 = new File(['content2'], 'file2.csv', {
      type: 'text/csv',
    })

    if (input) {
      fireEvent.change(input, { target: { files: [file2] } })
    }

    expect(screen.getByText('file2.csv')).toBeInTheDocument()
    expect(mockOnFileSelect).toHaveBeenCalledTimes(2)
  })

  it('should prevent interaction during upload', () => {
    const mockOnFileSelect = vi.fn()
    render(<FileUpload onFileSelect={mockOnFileSelect} isUploading={true} />)

    // Verify upload state is shown
    expect(screen.getByText(/processando arquivo/i)).toBeInTheDocument()
    expect(screen.getByText(/isso pode levar alguns segundos/i)).toBeInTheDocument()

    // Verify the file input is not accessible during upload
    const label = screen.queryByLabelText(/selecionar arquivo/i)
    expect(label).not.toBeInTheDocument()

    // Verify the drop zone shows loading state
    const loadingSpinner = document.querySelector('.animate-spin')
    expect(loadingSpinner).toBeInTheDocument()
  })
})
