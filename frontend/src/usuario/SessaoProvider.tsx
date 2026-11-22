import { useState, type ReactNode } from 'react'
import * as usuarios from './usuarioApi'
import { SessaoContexto } from './sessaoContexto'

export function SessaoProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<usuarios.Usuario | null>(null)

  async function entrar(email: string, senha: string) {
    setUsuario(await usuarios.entrar(email, senha))
  }

  async function sair() {
    await usuarios.sair()
    setUsuario(null)
  }

  return <SessaoContexto.Provider value={{ usuario, entrar, sair }}>{children}</SessaoContexto.Provider>
}