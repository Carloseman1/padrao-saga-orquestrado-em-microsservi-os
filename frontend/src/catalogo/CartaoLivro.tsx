import { Link } from 'react-router-dom'
import { CapaLivro } from './CapaLivro'
import type { Livro, Situacao } from './catalogoApi'
import { Preco } from './Preco'
import { SeloDisponibilidade } from './SeloDisponibilidade'
import estilos from './CartaoLivro.module.css'

const FRETE_GRATIS_A_PARTIR_DE = 99

export function CartaoLivro({ livro, situacao }: { livro: Livro; situacao: Situacao | undefined }) {
  return (
    <Link to={`/livros/${livro.codigo}`} className={estilos.cartao}>
      <div className={estilos.vitrine}>
        <CapaLivro titulo={livro.titulo} capaUrl={livro.capaUrl} />
      </div>
      <div className={estilos.info}>
        <h3 className={estilos.titulo}>{livro.titulo}</h3>
        {livro.autor && <p className={estilos.autor}>{livro.autor}</p>}
        <Preco valor={livro.preco} />
        {livro.preco >= FRETE_GRATIS_A_PARTIR_DE && <span className={estilos.frete}>Frete grátis</span>}
        <SeloDisponibilidade situacao={situacao} />
      </div>
    </Link>
  )
}
