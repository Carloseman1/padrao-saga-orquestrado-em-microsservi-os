import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ErroApi } from '../compartilhado/https'
import { useCarrinho } from '../carrinho/useCarrinho'
import { CapaLivro } from './CapaLivro'
import { Carrossel } from './Carrossel'
import { CartaoLivro } from './CartaoLivro'
import { buscarLivro, NOMES_DAS_CATEGORIAS, type Livro, type Situacao } from './catalogoApi'
import { Preco } from './Preco'
import { useCatalogo } from './useCatalogo'
import estilos from './LivroDetalhe.module.css'

const QUANTIDADE_MAXIMA = 5
const DIAS_UTEIS_PARA_ENTREGA = 5
const FRETE_GRATIS_A_PARTIR_DE = 99

const TEXTO_DA_SITUACAO: Record<Situacao, string> = {
  DISPONIVEL: 'Em estoque',
  ULTIMAS_UNIDADES: 'Últimas unidades em estoque',
  ESGOTADO: 'Esgotado no momento',
}

function dataDeEntrega(): string {
  const data = new Date()
  let diasUteis = 0
  while (diasUteis < DIAS_UTEIS_PARA_ENTREGA) {
    data.setDate(data.getDate() + 1)
    if (data.getDay() !== 0 && data.getDay() !== 6) diasUteis++
  }
  return data.toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' })
}

export function LivroDetalhe() {
  const { codigo = '' } = useParams()
  const { adicionar } = useCarrinho()
  const { livros, disponibilidade } = useCatalogo()
  const [livro, setLivro] = useState<Livro | null>(null)
  const [erro, setErro] = useState('')
  const [quantidade, setQuantidade] = useState(1)
  const [adicionado, setAdicionado] = useState(false)

  useEffect(() => {
    let ativo = true
    setLivro(null)
    setErro('')
    setAdicionado(false)
    setQuantidade(1)
    window.scrollTo(0, 0)

    buscarLivro(codigo)
      .then((encontrado) => ativo && setLivro(encontrado))
      .catch((falha) => ativo && setErro(falha instanceof ErroApi ? falha.message : 'Não foi possível carregar o livro.'))

    return () => {
      ativo = false
    }
  }, [codigo])

  if (erro) {
    return (
      <main className={estilos.pagina}>
        <p className={estilos.erro} role="alert">
          {erro}
        </p>
        <Link to="/" className={estilos.link}>
          Voltar ao catálogo
        </Link>
      </main>
    )
  }

  if (!livro) {
    return <main className={estilos.pagina}>Carregando...</main>
  }

  const situacao = disponibilidade.get(livro.codigo)
  const esgotado = situacao === 'ESGOTADO'
  const freteGratis = livro.preco >= FRETE_GRATIS_A_PARTIR_DE
  const relacionados = livros.filter((outro) => outro.categoria === livro.categoria && outro.codigo !== livro.codigo)

  function adicionarAoCarrinho() {
    if (!livro) return
    adicionar({ codigo: livro.codigo, titulo: livro.titulo, preco: livro.preco, capaUrl: livro.capaUrl }, quantidade)
    setAdicionado(true)
  }

  return (
    <main className={estilos.pagina}>
      <nav className={estilos.trilha} aria-label="Trilha">
        <Link to="/">Livros</Link>
        <span aria-hidden="true">›</span>
        <Link to={`/?categoria=${livro.categoria}`}>{NOMES_DAS_CATEGORIAS[livro.categoria]}</Link>
        <span aria-hidden="true">›</span>
        <span>{livro.titulo}</span>
      </nav>

      <div className={estilos.produto}>
        <div className={estilos.capa}>
          <CapaLivro titulo={livro.titulo} capaUrl={livro.capaUrl} tamanho="grande" />
        </div>

        <section className={estilos.info}>
          <h1 className={estilos.titulo}>{livro.titulo}</h1>
          {livro.autor && (
            <p className={estilos.autor}>
              por <Link to={`/?busca=${encodeURIComponent(livro.autor)}`}>{livro.autor}</Link> <span>(Autor)</span>
            </p>
          )}

          <hr className={estilos.divisor} />

          <Preco valor={livro.preco} tamanho="grande" />
          {freteGratis && <p className={estilos.frete}>Frete grátis para todo o Brasil</p>}

          {livro.descricao && (
            <>
              <h2 className={estilos.subtitulo}>Sobre o livro</h2>
              <p className={estilos.descricao}>{livro.descricao}</p>
            </>
          )}

          <h2 className={estilos.subtitulo}>Detalhes do produto</h2>
          <dl className={estilos.detalhes}>
            {livro.editora && (
              <>
                <dt>Editora</dt>
                <dd>{livro.editora}</dd>
              </>
            )}
            <dt>Idioma</dt>
            <dd>{livro.categoria === 'TECNOLOGIA' ? 'Inglês' : 'Português'}</dd>
            <dt>Categoria</dt>
            <dd>{NOMES_DAS_CATEGORIAS[livro.categoria]}</dd>
            <dt>Código</dt>
            <dd>{livro.codigo}</dd>
          </dl>
        </section>

        <aside className={estilos.caixaCompra} aria-label="Comprar">
          <Preco valor={livro.preco} tamanho="grande" />

          <p className={estilos.entrega}>
            {freteGratis ? 'Frete grátis. ' : ''}Entrega estimada: <strong>{dataDeEntrega()}</strong>
          </p>

          {situacao && (
            <p className={`${estilos.situacao} ${esgotado ? estilos.esgotado : ''} ${situacao === 'ULTIMAS_UNIDADES' ? estilos.ultimas : ''}`}>
              {TEXTO_DA_SITUACAO[situacao]}
            </p>
          )}

          <label className={estilos.quantidade}>
            Quantidade:
            <select value={quantidade} onChange={(e) => setQuantidade(Number(e.target.value))} disabled={esgotado}>
              {Array.from({ length: QUANTIDADE_MAXIMA }, (_, i) => i + 1).map((n) => (
                <option key={n} value={n}>
                  {n}
                </option>
              ))}
            </select>
          </label>

          <button type="button" className={estilos.botao} onClick={adicionarAoCarrinho} disabled={esgotado}>
            {esgotado ? 'Indisponível' : 'Adicionar ao carrinho'}
          </button>

          {adicionado && (
            <p className={estilos.sucesso} role="status">
              ✓ Adicionado ao carrinho
            </p>
          )}

          <dl className={estilos.vendedor}>
            <dt>Enviado por</dt>
            <dd>Livraria</dd>
            <dt>Vendido por</dt>
            <dd>Livraria</dd>
            <dt>Pagamento</dt>
            <dd>Transação segura</dd>
          </dl>
        </aside>
      </div>

      {relacionados.length > 0 && (
        <Carrossel titulo={`Mais livros de ${NOMES_DAS_CATEGORIAS[livro.categoria]}`}>
          {relacionados.map((outro) => (
            <CartaoLivro key={outro.codigo} livro={outro} situacao={disponibilidade.get(outro.codigo)} />
          ))}
        </Carrossel>
      )}
    </main>
  )
}
