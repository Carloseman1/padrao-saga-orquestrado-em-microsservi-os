import { useState, type FormEvent } from 'react'
import { ErroApi } from '../compartilhado/https'
import { buscarEndereco, cadastrar, type Endereco, type EnderecoInformado } from './usuarioApi'
import { useSessao } from './useSessao'
import estilos from './Formulario.module.css'

const ENDERECO_VAZIO: EnderecoInformado = { rua: '', bairro: '', cidade: '', estado: '' }
const CEP_FORA_DO_AR = ['CEP_INDISPONIVEL', 'CEP_TIMEOUT']

type Props = {
  aoCadastrar: () => void
  aoVoltar: () => void
}

export function FormularioCadastro({ aoCadastrar, aoVoltar }: Props) {
  const { entrar } = useSessao()
  const [nome, setNome] = useState('')
  const [sobrenome, setSobrenome] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [cep, setCep] = useState('')
  const [numero, setNumero] = useState('')
  const [complemento, setComplemento] = useState('')
  const [endereco, setEndereco] = useState<Endereco | null>(null)
  const [digitarEndereco, setDigitarEndereco] = useState(false)
  const [enderecoDigitado, setEnderecoDigitado] = useState<EnderecoInformado>(ENDERECO_VAZIO)
  const [erroCep, setErroCep] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  async function consultarCep() {
    const digitos = cep.replace(/\D/g, '')
    setEndereco(null)
    setErroCep('')
    if (digitos.length !== 8) return

    try {
      setEndereco(await buscarEndereco(digitos))
      setDigitarEndereco(false)
    } catch (falha) {
      if (falha instanceof ErroApi && CEP_FORA_DO_AR.includes(falha.codigo)) {
        setDigitarEndereco(true)
        setErroCep('Não conseguimos consultar o CEP agora. Preencha o endereço abaixo.')
      } else {
        setErroCep(falha instanceof ErroApi ? falha.message : 'CEP não encontrado.')
      }
    }
  }

  function alterarEndereco(campo: keyof EnderecoInformado, valor: string) {
    setEnderecoDigitado((atual) => ({ ...atual, [campo]: valor }))
  }

  async function enviar(evento: FormEvent) {
    evento.preventDefault()
    setErro('')
    setEnviando(true)

    try {
      await cadastrar({
        nome,
        sobrenome,
        email,
        senha,
        cep,
        numero,
        complemento,
        enderecoInformado: digitarEndereco ? enderecoDigitado : null,
      })
      await entrar(email, senha)
      aoCadastrar()
    } catch (falha) {
      setErro(falha instanceof ErroApi ? falha.message : 'Não foi possível criar a conta. Tente de novo.')
    } finally {
      setEnviando(false)
    }
  }

  return (
    <form className={estilos.formulario} onSubmit={enviar}>
      <div className={estilos.linha}>
        <label className={estilos.campo}>
          Nome
          <input value={nome} onChange={(e) => setNome(e.target.value)} required autoComplete="given-name" autoFocus />
        </label>
        <label className={estilos.campo}>
          Sobrenome
          <input value={sobrenome} onChange={(e) => setSobrenome(e.target.value)} required autoComplete="family-name" />
        </label>
      </div>

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
          autoComplete="new-password"
        />
        <span className={estilos.dica}>Mínimo de 8 caracteres, com maiúscula, minúscula, número e símbolo (@#$%!*).</span>
      </label>

      <div className={estilos.linhaCep}>
        <label className={estilos.campo}>
          CEP
          <input
            value={cep}
            onChange={(e) => setCep(e.target.value)}
            onBlur={consultarCep}
            required
            inputMode="numeric"
            autoComplete="postal-code"
            placeholder="00000-000"
          />
        </label>
        <label className={estilos.campo}>
          Número
          <input value={numero} onChange={(e) => setNumero(e.target.value)} required />
        </label>
      </div>

      {endereco && (
        <p className={estilos.endereco}>
          {endereco.rua}, {endereco.bairro} · {endereco.cidade}/{endereco.estado}
        </p>
      )}
      {erroCep && <p className={estilos.dica}>{erroCep}</p>}

      {digitarEndereco && (
        <>
          <label className={estilos.campo}>
            Rua
            <input value={enderecoDigitado.rua} onChange={(e) => alterarEndereco('rua', e.target.value)} required />
          </label>
          <div className={estilos.linha}>
            <label className={estilos.campo}>
              Bairro
              <input value={enderecoDigitado.bairro} onChange={(e) => alterarEndereco('bairro', e.target.value)} required />
            </label>
            <label className={estilos.campo}>
              Cidade
              <input value={enderecoDigitado.cidade} onChange={(e) => alterarEndereco('cidade', e.target.value)} required />
            </label>
          </div>
          <label className={estilos.campo}>
            Estado (UF)
            <input
              value={enderecoDigitado.estado}
              onChange={(e) => alterarEndereco('estado', e.target.value)}
              required
              maxLength={2}
            />
          </label>
        </>
      )}

      <label className={estilos.campo}>
        Complemento (opcional)
        <input value={complemento} onChange={(e) => setComplemento(e.target.value)} />
      </label>

      {erro && (
        <p className={estilos.erro} role="alert">
          {erro}
        </p>
      )}

      <button className={estilos.botao} type="submit" disabled={enviando}>
        {enviando ? 'Criando a conta...' : 'Criar conta'}
      </button>

      <div className={estilos.alternativas}>
        <button type="button" className={estilos.link} onClick={aoVoltar}>
          Já tenho conta. Entrar
        </button>
      </div>
    </form>
  )
}
