import { useState, type FormEvent } from 'react'
import { ErroApi } from '../compartilhado/https'
import { redefinirSenha, solicitarRecuperacao } from './usuarioApi'
import estilos from './Formulario.module.css'

type Props = {
  aoRedefinir: () => void
  aoVoltar: () => void
}

export function FormularioRecuperacao({ aoRedefinir, aoVoltar }: Props) {
  const [etapa, setEtapa] = useState<'email' | 'codigo'>('email')
  const [email, setEmail] = useState('')
  const [codigo, setCodigo] = useState('')
  const [senha, setSenha] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function executar(acao: () => Promise<void>) {
    setErro('')
    setEnviando(true)
    try {
      await acao()
    } catch (falha) {
      setErro(falha instanceof ErroApi ? falha.message : 'Algo deu errado. Tente de novo.')
    } finally {
      setEnviando(false)
    }
  }

  function pedirCodigo(evento: FormEvent) {
    evento.preventDefault()
    executar(async () => {
      await solicitarRecuperacao(email)
      setEtapa('codigo')
    })
  }

  function trocarSenha(evento: FormEvent) {
    evento.preventDefault()
    executar(async () => {
      await redefinirSenha(codigo.trim(), senha)
      aoRedefinir()
    })
  }

  if (etapa === 'email') {
    return (
      <form className={estilos.formulario} onSubmit={pedirCodigo}>
        <p className={estilos.dica}>Informe o e-mail da sua conta. Vamos gerar um código para você criar uma senha nova.</p>

        <label className={estilos.campo}>
          E-mail
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus />
        </label>

        {erro && (
          <p className={estilos.erro} role="alert">
            {erro}
          </p>
        )}

        <button className={estilos.botao} type="submit" disabled={enviando}>
          {enviando ? 'Enviando...' : 'Continuar'}
        </button>

        <div className={estilos.alternativas}>
          <button type="button" className={estilos.link} onClick={aoVoltar}>
            Voltar para o login
          </button>
        </div>
      </form>
    )
  }

  return (
    <form className={estilos.formulario} onSubmit={trocarSenha}>
      <p className={estilos.sucesso} role="status">
        Se existir uma conta com {email}, um código de recuperação foi gerado. Ele vale por tempo limitado.
      </p>
      <p className={estilos.dica}>Ambiente de demonstração: o código aparece no log do usuario-service.</p>

      <label className={estilos.campo}>
        Código de recuperação
        <input value={codigo} onChange={(e) => setCodigo(e.target.value)} required autoFocus autoComplete="one-time-code" />
      </label>

      <label className={estilos.campo}>
        Nova senha
        <input
          type="password"
          value={senha}
          onChange={(e) => setSenha(e.target.value)}
          required
          autoComplete="new-password"
        />
        <span className={estilos.dica}>Mínimo de 8 caracteres, com maiúscula, minúscula, número e símbolo (@#$%!*).</span>
      </label>

      {erro && (
        <p className={estilos.erro} role="alert">
          {erro}
        </p>
      )}

      <button className={estilos.botao} type="submit" disabled={enviando}>
        {enviando ? 'Salvando...' : 'Salvar nova senha'}
      </button>

      <div className={estilos.alternativas}>
        <button type="button" className={estilos.link} onClick={aoVoltar}>
          Voltar para o login
        </button>
      </div>
    </form>
  )
}
