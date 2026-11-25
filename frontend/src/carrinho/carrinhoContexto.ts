import { createContext } from 'react'

export type ItemCarrinho = {
  codigo: string
  titulo: string
  preco: number
  capaUrl: string | null
  quantidade: number
}

export type Carrinho = {
  itens: ItemCarrinho[]
  quantidadeTotal: number
  adicionar: (item: Omit<ItemCarrinho, 'quantidade'>, quantidade: number) => void
}

export const CarrinhoContexto = createContext<Carrinho | null>(null)
