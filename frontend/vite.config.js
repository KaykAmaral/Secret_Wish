import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Configuracao do Vite para compilar React e expor globalThis a libs que esperam "global".
export default defineConfig({
  plugins: [react()],
  define: {
    global: 'globalThis',
  },
})
