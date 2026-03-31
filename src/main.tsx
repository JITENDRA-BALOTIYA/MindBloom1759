import React from 'react'
import ReactDOM from 'react-dom/client'
import './styles/global.css'
import App from './App.tsx'
import { AuthProvider } from './hooks/AuthProvider'
import { ThemeWrapper } from './theme/ThemeWrapper'

const rootElement = document.getElementById('root')
if (!rootElement) throw new Error('Failed to find the root element')

ReactDOM.createRoot(rootElement).render(
  <React.StrictMode>
    <ThemeWrapper>
      <AuthProvider>
        <App />
      </AuthProvider>
    </ThemeWrapper>
  </React.StrictMode>,
)
