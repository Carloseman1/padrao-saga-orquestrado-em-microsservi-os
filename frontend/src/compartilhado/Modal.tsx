import { useEffect, useRef, type ReactNode } from 'react'
import estilos from './Modal.module.css'

type Props = {
  titulo: string
  aberto: boolean
  aoFechar: () => void
  largo?: boolean
  children: ReactNode
}

export function Modal({ titulo, aberto, aoFechar, largo = false, children }: Props) {
  const dialogo = useRef<HTMLDialogElement>(null)

  useEffect(() => {
    const elemento = dialogo.current
    if (!elemento) return

    if (aberto && !elemento.open) {
      elemento.showModal()
    } else if (!aberto && elemento.open) {
      elemento.close()
    }
  }, [aberto])

  return (
    <dialog
      ref={dialogo}
      className={`${estilos.modal} ${largo ? estilos.largo : ''}`}
      aria-label={titulo}
      onClose={aoFechar}
      onClick={(evento) => evento.target === dialogo.current && aoFechar()}
    >
      <div className={estilos.conteudo}>
        <header className={estilos.topo}>
          <h2 className={estilos.titulo}>{titulo}</h2>
          <button type="button" className={estilos.fechar} onClick={aoFechar} aria-label="Fechar">
            ×
          </button>
        </header>
        {children}
      </div>
    </dialog>
  )
}
