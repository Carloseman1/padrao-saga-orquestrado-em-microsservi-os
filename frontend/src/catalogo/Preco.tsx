import estilos from './Preco.module.css'

export function Preco({ valor, tamanho = 'normal' }: { valor: number; tamanho?: 'normal' | 'grande' }) {
  const [inteiro, centavos] = valor.toFixed(2).split('.')

  return (
    <span className={`${estilos.preco} ${tamanho === 'grande' ? estilos.grande : ''}`} aria-label={`R$ ${inteiro},${centavos}`}>
      <span className={estilos.simbolo}>R$</span>
      <span className={estilos.inteiro}>{Number(inteiro).toLocaleString('pt-BR')}</span>
      <span className={estilos.centavos}>{centavos}</span>
    </span>
  )
}
