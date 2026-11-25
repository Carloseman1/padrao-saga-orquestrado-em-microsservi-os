import type { Situacao } from './catalogoApi'
import estilos from './SeloDisponibilidade.module.css'

export function SeloDisponibilidade({ situacao }: { situacao: Situacao | undefined }) {
  if (situacao === 'ESGOTADO') {
    return <span className={`${estilos.selo} ${estilos.esgotado}`}>Esgotado</span>
  }

  if (situacao === 'ULTIMAS_UNIDADES') {
    return <span className={`${estilos.selo} ${estilos.ultimas}`}>Últimas unidades</span>
  }

  return null
}
