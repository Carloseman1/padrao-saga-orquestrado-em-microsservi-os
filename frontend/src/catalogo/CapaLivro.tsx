import { useEffect, useState } from 'react'
import estilos from './CapaLivro.module.css'

type Props = {
  titulo: string
  capaUrl: string | null
  tamanho?: 'normal' | 'grande'
}

const CAPA_GRANDE = '-L.jpg'
const CAPA_MEDIA = '-M.jpg'

export function CapaLivro({ titulo, capaUrl, tamanho = 'normal' }: Props) {
  const media = capaUrl?.replace(CAPA_GRANDE, CAPA_MEDIA) ?? null
  const [url, setUrl] = useState(media)
  const classe = `${estilos.capa} ${tamanho === 'grande' ? estilos.grande : ''}`

  useEffect(() => {
    setUrl(media)
    if (tamanho !== 'grande' || !capaUrl || capaUrl === media) return

    let ativo = true
    const imagem = new Image()
    imagem.onload = () => ativo && setUrl(capaUrl)
    imagem.src = capaUrl

    return () => {
      ativo = false
    }
  }, [capaUrl, media, tamanho])

  if (!url) {
    return (
      <div className={`${classe} ${estilos.semCapa}`} aria-label={`Capa de ${titulo}`}>
        <span>{titulo}</span>
      </div>
    )
  }

  return <img className={classe} src={url} alt={`Capa de ${titulo}`} loading="lazy" onError={() => setUrl(null)} />
}
