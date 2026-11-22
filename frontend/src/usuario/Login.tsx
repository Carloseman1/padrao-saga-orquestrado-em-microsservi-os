import { useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { ErroApi } from '../compartilhado/https'
import { useSessao } from './useSessao'
import estilos from './Login.module.css'

export function Login() {
  const { entrar } = useSessao()
  const navegar = useNavigate()
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
      navegar('/')
    } catch (falha) {
      setErro(falha instanceof ErroApi ? falha.message : 'Não foi possível entrar. Tente de novo.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className={estilos.pagina}>
      <form className={estilos.cartao} onSubmit={enviar}>
        <h1 className={estilos.titulo}>Entrar</h1>

        <label className={estilos.campo}>
          E-mail
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoComplete="email" />
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

        {erro && (
          <p className={estilos.erro} role="alert">
            {erro}
          </p>
        )}

        <button className={estilos.botao} type="submit" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
    </main>
  )
}