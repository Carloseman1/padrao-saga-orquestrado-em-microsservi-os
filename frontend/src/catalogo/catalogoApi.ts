import { chamarApi } from '../compartilhado/https'

export type Categoria = 'TECNOLOGIA' | 'LITERATURA_BRASILEIRA' | 'HISTORIA' | 'OUTROS'

export type Situacao = 'DISPONIVEL' | 'ULTIMAS_UNIDADES' | 'ESGOTADO'

export type Livro = {
  codigo: string
  titulo: string
  preco: number
  autor: string | null
  editora: string | null
  categoria: Categoria
  descricao: string | null
  capaUrl: string | null
}

type Disponibilidade = {
  codigoLivro: string
  situacao: Situacao
}

export const NOMES_DAS_CATEGORIAS: Record<Categoria, string> = {
  TECNOLOGIA: 'Tecnologia',
  LITERATURA_BRASILEIRA: 'Literatura brasileira',
  HISTORIA: 'História',
  OUTROS: 'Outros',
}

export const ORDEM_DAS_CATEGORIAS: Categoria[] = ['TECNOLOGIA', 'LITERATURA_BRASILEIRA', 'HISTORIA', 'OUTROS']

export function listarLivros(): Promise<Livro[]> {
  return chamarApi<Livro[]>('GET', '/api/livros')
}

export function buscarLivro(codigo: string): Promise<Livro> {
  return chamarApi<Livro>('GET', `/api/livros/${encodeURIComponent(codigo)}`)
}

export async function buscarDisponibilidade(): Promise<Map<string, Situacao>> {
  const lista = await chamarApi<Disponibilidade[]>('GET', '/api/estoques/disponibilidade')
  return new Map(lista.map((item) => [item.codigoLivro, item.situacao]))
}
