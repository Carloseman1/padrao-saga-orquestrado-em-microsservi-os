import { useRef, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import estilos from './Carrossel.module.css'

type Props = {
  titulo: string
  linkVerTodos?: string
  children: ReactNode
}

export function Carrossel({ titulo, linkVerTodos, children }: Props) {
  const trilho = useRef<HTMLDivElement>(null)

  function rolar(direcao: 1 | -1) {
    const elemento = trilho.current
    if (elemento) {
      elemento.scrollBy({ left: direcao * elemento.clientWidth * 0.8, behavior: 'smooth' })
    }
  }

  return (
    <section className={estilos.carrossel}>
      <header className={estilos.topo}>
        <h2 className={estilos.titulo}>{titulo}</h2>
        {linkVerTodos && (
          <Link to={linkVerTodos} className={estilos.verTodos}>
            Ver todos
          </Link>
        )}
      </header>
      <div className={estilos.area}>
        <button type="button" className={`${estilos.seta} ${estilos.esquerda}`} onClick={() => rolar(-1)} aria-label="Anteriores">
          ‹
        </button>
        <div ref={trilho} className={estilos.trilho}>
          {children}
        </div>
        <button type="button" className={`${estilos.seta} ${estilos.direita}`} onClick={() => rolar(1)} aria-label="Próximos">
          ›
        </button>
      </div>
    </section>
  )
}
