import { useEffect, useState } from 'react'
import { buscarDisponibilidade, listarLivros, type Livro, type Situacao } from './catalogoApi'

export function useCatalogo() {
  const [livros, setLivros] = useState<Livro[]>([])
  const [disponibilidade, setDisponibilidade] = useState<Map<string, Situacao>>(new Map())
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true

    listarLivros()
      .then((lista) => ativo && setLivros(lista))
      .catch(() => ativo && setErro('Não foi possível carregar o catálogo. Tente de novo em instantes.'))
      .finally(() => ativo && setCarregando(false))

    buscarDisponibilidade()
      .then((mapa) => ativo && setDisponibilidade(mapa))
      .catch(() => undefined)

    return () => {
      ativo = false
    }
  }, [])

  return { livros, disponibilidade, carregando, erro }
}
