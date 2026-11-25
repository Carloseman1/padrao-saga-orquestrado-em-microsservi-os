import { useState, type FormEvent } from 'react'
import { ErroApi } from '../compartilhado/https'
import { useSessao } from './useSessao'
import estilos from './Formulario.module.css'

type Props = {
  aoEntrar: () => void
  aoEsquecerSenha?: () => void
  aoCriarConta?: () => void
  aviso?: string
}

export function FormularioLogin({ aoEntrar, aoEsquecerSenha, aoCriarConta, aviso }: Props) {
  const { entrar } = useSessao()
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro('')
    setEnviando(true)

    try {
      await entrar(email, senha)
      aoEntrar()
    } catch (falha) {
      setErro(falha instanceof ErroApi ? falha.message : 'Não foi possível entrar. Tente de novo.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className={estilos.formulario} onSubmit={enviar}>
      {aviso && (
        <p className={estilos.sucesso} role="status">
          {aviso}
        </p>
      )}

      <label className={estilos.campo}>
        E-mail
        <input
          type="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          autoComplete="email"
          autoFocus
        />
      </label>

      <label className={estilos.campo}>
        Senha
        <input
          type="password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          required
          autoComplete="current-password"
        />
      </label>

      {aoEsquecerSenha && (
        <button type="button" className={estilos.link} onClick={aoEsquecerSenha} style={{ alignSelf: 'flex-end' }}>
          Esqueci minha senha
        </button>
      )}

      {erro && (
        <p className={estilos.erro} role="alert">
          {erro}
        </p>
      )}

      <button className={estilos.botao} type="submit" disabled={enviando}>
        {enviando ? 'Entrando...' : 'Entrar'}
      </button>

      {aoCriarConta && (
        <div className={estilos.alternativas}>
          <p>Ainda não tem conta?</p>
          <button type="button" className={estilos.botaoSecundario} onClick={aoCriarConta}>
            Criar sua conta
          </button>
        </div>
      )}
    </form>
  )
}
