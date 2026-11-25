import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './compartilhado/estilos.css'
import App from './App.tsx'
import { CarrinhoProvider } from './carrinho/CarrinhoProvider'
import { SessaoProvider } from './usuario/SessaoProvider'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <BrowserRouter>
      <SessaoProvider>
        <CarrinhoProvider>
          <App />
        </CarrinhoProvider>
      </SessaoProvider>
    </BrowserRouter>
  </StrictMode>,
)