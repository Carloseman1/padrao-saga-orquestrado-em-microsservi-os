import { useEffect, useState, type ReactNode } from 'react'
import { CarrinhoContexto, type ItemCarrinho } from './carrinhoContexto'

const CHAVE = 'livraria.carrinho'

function lerCarrinhoSalvo(): ItemCarrinho[] {
  try {
    const salvo = localStorage.getItem(CHAVE)
    return salvo ? (JSON.parse(salvo) as ItemCarrinho[]) : []
  } catch {
    return []
  }
}

export function CarrinhoProvider({ children }: { children: ReactNode }) {
  const [itens, setItens] = useState<ItemCarrinho[]>(lerCarrinhoSalvo)

  useEffect(() => {
    try {
      localStorage.setItem(CHAVE, JSON.stringify(itens))
    } catch {
    }
  }, [itens])

  function adicionar(item: Omit<ItemCarrinho, 'quantidade'>, quantidade: number) {
    setItens((atuais) => {
      const existente = atuais.find((atual) => atual.codigo === item.codigo)
      if (existente) {
        return atuais.map((atual) =>
          atual.codigo === item.codigo ? { ...atual, quantidade: atual.quantidade + quantidade } : atual,
        )
      }
      return [...atuais, { ...item, quantidade }]
    })
  }

  const quantidadeTotal = itens.reduce((total, item) => total + item.quantidade, 0)

  return <CarrinhoContexto.Provider value={{ itens, quantidadeTotal, adicionar }}>{children}</CarrinhoContexto.Provider>
}
