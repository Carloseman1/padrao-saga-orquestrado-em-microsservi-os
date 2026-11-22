import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const servicos = {
  pedido: 'http://localhost:3000',
  usuario: 'http://localhost:8093',
  pagamento: 'http://localhost:8091',
  catalogo: 'http://localhost:8090',
  estoque: 'http://localhost:8092',
}

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/pedidos': servicos.pedido,
      '/api/eventos': servicos.pedido,
      '/api/usuarios': servicos.usuario,
      '/api/endereco': servicos.usuario,
      '/api/sessao': servicos.usuario,
      '/api/cartoes': servicos.pagamento,
      '/api/livros': servicos.catalogo,
      '/api/estoques': servicos.estoque,
    },
  },
})
