import { useState, type FormEvent } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { NOMES_DAS_CATEGORIAS, ORDEM_DAS_CATEGORIAS } from '../catalogo/catalogoApi'
import { useCarrinho } from '../carrinho/useCarrinho'
import { useSessao } from '../usuario/useSessao'
import { IconeBusca, IconeCarrinho } from './Icones'
import estilos from './Cabecalho.module.css'

export function Cabecalho() {
  const { usuario, sair, abrirLogin } = useSessao()
  const { quantidadeTotal } = useCarrinho()
  const [parametros] = useSearchParams()
  const [busca, setBusca] = useState(parametros.get('busca') ?? '')
  const navegar = useNavigate()

  function buscar(evento: FormEvent) {
    evento.preventDefault()
    const termo = busca.trim()
    navegar(termo ? `/?busca=${encodeURIComponent(termo)}` : '/')
  }

  return (
    <header className={estilos.cabecalho}>
      <div className={estilos.barra}>
        <Link to="/" className={estilos.marca} onClick={() => setBusca('')}>
          Livraria<span className={estilos.ponto}>.</span>
        </Link>

        <form className={estilos.busca} onSubmit={buscar} role="search">
          <input
            type="search"
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            placeholder="Busque por título ou autor"
            aria-label="Buscar livros"
          />
          <button type="submit" aria-label="Buscar">
            <IconeBusca />
          </button>
        </form>

        <div className={estilos.conta}>
          {usuario ? (
            <>
              <span className={estilos.linhaPequena}>Olá, {usuario.nome.split(' ')[0]}</span>
              <button type="button" className={estilos.linhaForte} onClick={() => sair()}>
                Sair
              </button>
            </>
          ) : (
            <button type="button" className={estilos.botaoConta} onClick={abrirLogin}>
              <span className={estilos.linhaPequena}>Olá, faça seu login</span>
              <span className={estilos.linhaForte}>Entrar</span>
            </button>
          )}
        </div>

        <span className={estilos.carrinho} aria-label={`Carrinho com ${quantidadeTotal} itens`}>
          <span className={estilos.iconeCarrinho}>
            <IconeCarrinho />
            <span className={estilos.contador}>{quantidadeTotal}</span>
          </span>
          <span className={estilos.linhaForte}>Carrinho</span>
        </span>
      </div>

      <nav className={estilos.categorias} aria-label="Categorias">
        <Link to="/">Início</Link>
        {ORDEM_DAS_CATEGORIAS.filter((categoria) => categoria !== 'OUTROS').map((categoria) => (
          <Link key={categoria} to={`/?categoria=${categoria}`}>
            {NOMES_DAS_CATEGORIAS[categoria]}
          </Link>
        ))}
        <span className={estilos.aviso}>Frete grátis em compras acima de R$ 99</span>
      </nav>
    </header>
  )
}
