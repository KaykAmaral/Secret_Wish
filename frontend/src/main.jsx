import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.jsx'

/**
 * Ponto de entrada principal da aplicação Secret Wish.
 * 
 * Este arquivo inicializa o ecossistema React, conectando o componente raiz <App />
 * ao elemento DOM com id 'root' definido no index.html.
 */
createRoot(document.getElementById('root')).render(
  /**
   * <StrictMode> é uma ferramenta para destacar problemas potenciais na aplicação.
   * Em desenvolvimento, ele ajuda a detectar efeitos colaterais inesperados e APIs obsoletas.
   */
  <StrictMode>
    <App />
  </StrictMode>,
)
