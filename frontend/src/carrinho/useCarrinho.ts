import { useContext } from 'react'
import { CarrinhoContexto } from './carrinhoContexto'

export function useCarrinho() {
  const carrinho = useContext(CarrinhoContexto)

  if (!carrinho) {
    throw new Error('useCarrinho precisa estar dentro do CarrinhoProvider')
  }

  return carrinho
}
