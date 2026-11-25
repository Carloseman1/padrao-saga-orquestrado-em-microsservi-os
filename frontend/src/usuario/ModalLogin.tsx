import { useState } from 'react'
import { Modal } from '../compartilhado/Modal'
import { FormularioCadastro } from './FormularioCadastro'
import { FormularioLogin } from './FormularioLogin'
import { FormularioRecuperacao } from './FormularioRecuperacao'

type Tela = 'entrar' | 'cadastrar' | 'recuperar'

const TITULOS: Record<Tela, string> = {
  entrar: 'Entrar na sua conta',
  cadastrar: 'Criar sua conta',
  recuperar: 'Recuperar a senha',
}

export function ModalLogin({ aberto, aoFechar }: { aberto: boolean; aoFechar: () => void }) {
  const [tela, setTela] = useState<Tela>('entrar')
  const [aviso, setAviso] = useState('')

  function fechar() {
    aoFechar()
    setTela('entrar')
    setAviso('')
  }

  function irPara(proxima: Tela, mensagem = '') {
    setTela(proxima)
    setAviso(mensagem)
  }

  return (
    <Modal titulo={TITULOS[tela]} aberto={aberto} aoFechar={fechar} largo={tela === 'cadastrar'}>
      {aberto && tela === 'entrar' && (
        <FormularioLogin
          aviso={aviso}
          aoEntrar={fechar}
          aoEsquecerSenha={() => irPara('recuperar')}
          aoCriarConta={() => irPara('cadastrar')}
        />
      )}
      {aberto && tela === 'cadastrar' && <FormularioCadastro aoCadastrar={fechar} aoVoltar={() => irPara('entrar')} />}
      {aberto && tela === 'recuperar' && (
        <FormularioRecuperacao
          aoRedefinir={() => irPara('entrar', 'Senha alterada. Entre com a senha nova.')}
          aoVoltar={() => irPara('entrar')}
        />
      )}
    </Modal>
  )
}
