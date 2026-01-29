import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import App from './App'

// Mock the auth store
vi.mock('@/stores/authStore', () => ({
  useAuthStore: () => ({
    initializeAuth: vi.fn(),
    isAuthenticated: false,
  }),
}))

// Mock react-hot-toast
vi.mock('react-hot-toast', () => ({
  Toaster: () => null,
  toast: {
    success: vi.fn(),
    error: vi.fn(),
  },
}))

// Mock ConfirmDialog
vi.mock('@/components/ConfirmDialog', () => ({
  default: () => null,
}))

describe('App', () => {
  it('renders login page when not authenticated', () => {
    render(
      <BrowserRouter>
        <App />
      </BrowserRouter>
    )
    
    expect(screen.getByText('Bem-vindo de volta')).toBeInTheDocument()
  })
})