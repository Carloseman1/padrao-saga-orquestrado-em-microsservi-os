import { Link, useSearchParams } from 'react-router-dom'
import { CapaLivro } from './CapaLivro'
import { Carrossel } from './Carrossel'
import { CartaoLivro } from './CartaoLivro'
import { NOMES_DAS_CATEGORIAS, ORDEM_DAS_CATEGORIAS, type Categoria, type Livro, type Situacao } from './catalogoApi'
import { useCatalogo } from './useCatalogo'
import estilos from './Catalogo.module.css'

const LIVRO_EM_DESTAQUE = 'BUILDING_MICROSERVICES'

export function Catalogo() {
  const { livros, disponibilidade, carregando, erro } = useCatalogo()
  const [parametros] = useSearchParams()
  const busca = parametros.get('busca')?.trim() ?? ''
  const categoria = parametros.get('categoria') as Categoria | null

  if (carregando) {
    return (
      <main className={estilos.pagina}>
        <p className={estilos.aviso}>Carregando o catálogo...</p>
      </main>
    )
  }

  if (erro) {
    return (
      <main className={estilos.pagina}>
        <p className={estilos.erro} role="alert">
          {erro}
        </p>
      </main>
    )
  }

  if (busca || categoria) {
    return <Resultados livros={livros} disponibilidade={disponibilidade} busca={busca} categoria={categoria} />
  }

  const categorias = ORDEM_DAS_CATEGORIAS.filter((c) => livros.some((livro) => livro.categoria === c))
  const destaque = livros.find((livro) => livro.codigo === LIVRO_EM_DESTAQUE)

  return (
    <main>
      {destaque && <Destaque livro={destaque} />}

      <div className={`${estilos.pagina} ${destaque ? estilos.sobreDestaque : ''}`}>
        <section className={estilos.blocos}>
          {categorias.map((c) => (
            <Link key={c} to={`/?categoria=${c}`} className={estilos.bloco}>
              <h2 className={estilos.tituloBloco}>{NOMES_DAS_CATEGORIAS[c]}</h2>
              <div className={estilos.mosaico}>
                {livros
                  .filter((livro) => livro.categoria === c)
                  .slice(0, 4)
                  .map((livro) => (
                    <CapaLivro key={livro.codigo} titulo={livro.titulo} capaUrl={livro.capaUrl} />
                  ))}
              </div>
              <span className={estilos.verMais}>Ver todos</span>
            </Link>
          ))}
        </section>

        {categorias.map((c) => (
          <Carrossel key={c} titulo={`Destaques em ${NOMES_DAS_CATEGORIAS[c]}`} linkVerTodos={`/?categoria=${c}`}>
            {livros
              .filter((livro) => livro.categoria === c)
              .map((livro) => (
                <CartaoLivro key={livro.codigo} livro={livro} situacao={disponibilidade.get(livro.codigo)} />
              ))}
          </Carrossel>
        ))}
      </div>
    </main>
  )
}

function Destaque({ livro }: { livro: Livro }) {
  return (
    <section className={estilos.destaque}>
      <div className={estilos.destaqueConteudo}>
        <div className={estilos.destaqueTexto}>
          <span className={estilos.etiqueta}>Destaque em {NOMES_DAS_CATEGORIAS[livro.categoria]}</span>
          <h1 className={estilos.destaqueTitulo}>{livro.titulo}</h1>
          {livro.autor && <p className={estilos.destaqueAutor}>por {livro.autor}</p>}
          {livro.descricao && <p className={estilos.destaqueDescricao}>{livro.descricao}</p>}
          <Link to={`/livros/${livro.codigo}`} className={estilos.destaqueBotao}>
            Ver o livro
          </Link>
        </div>
        <div className={estilos.destaqueCapa}>
          <CapaLivro titulo={livro.titulo} capaUrl={livro.capaUrl} tamanho="grande" />
        </div>
      </div>
    </section>
  )
}

type PropsResultados = {
  livros: Livro[]
  disponibilidade: Map<string, Situacao>
  busca: string
  categoria: Categoria | null
}

function Resultados({ livros, disponibilidade, busca, categoria }: PropsResultados) {
  const termo = busca.toLowerCase()
  const encontrados = livros.filter(
    (livro) =>
      (!categoria || livro.categoria === categoria) &&
      (!termo || livro.titulo.toLowerCase().includes(termo) || (livro.autor ?? '').toLowerCase().includes(termo)),
  )

  const plural = encontrados.length === 1 ? 'resultado' : 'resultados'
  const titulo = busca
    ? `${encontrados.length} ${plural} para "${busca}"`
    : NOMES_DAS_CATEGORIAS[categoria ?? 'OUTROS']

  return (
    <main className={estilos.pagina}>
      <div className={estilos.cabecalhoResultados}>
        <h1 className={estilos.tituloResultados}>{titulo}</h1>
        <Link to="/" className={estilos.limpar}>
          Voltar ao início
        </Link>
      </div>

      {encontrados.length === 0 ? (
        <p className={estilos.aviso}>Nenhum livro encontrado. Tente outro título ou autor.</p>
      ) : (
        <div className={estilos.grade}>
          {encontrados.map((livro) => (
            <CartaoLivro key={livro.codigo} livro={livro} situacao={disponibilidade.get(livro.codigo)} />
          ))}
        </div>
      )}
    </main>
  )
}
