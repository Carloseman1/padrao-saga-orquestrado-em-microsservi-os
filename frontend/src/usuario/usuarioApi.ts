import { chamarApi } from '../compartilhado/https'

export type Usuario = {
  id: number
  nome: string
  email: string
}

export type Endereco = {
  cep: string
  rua: string
  bairro: string
  cidade: string
  estado: string
}

export type EnderecoInformado = Omit<Endereco, 'cep'>

export type NovoUsuario = {
  nome: string
  sobrenome: string
  email: string
  senha: string
  cep: string
  numero: string
  complemento: string
  enderecoInformado: EnderecoInformado | null
}

export function entrar(email: string, senha: string): Promise<Usuario> {
  return chamarApi<Usuario>('POST', '/api/usuarios/login', { email, senha })
}

export function sair(): Promise<void> {
  return chamarApi<void>('DELETE', '/api/sessao')
}

export function cadastrar(usuario: NovoUsuario): Promise<void> {
  return chamarApi<void>('POST', '/api/usuarios', usuario)
}

export function buscarEndereco(cep: string): Promise<Endereco> {
  return chamarApi<Endereco>('GET', `/api/endereco/${encodeURIComponent(cep)}`)
}

export function solicitarRecuperacao(email: string): Promise<void> {
  return chamarApi<void>('POST', '/api/usuarios/esqueci-senha', { email })
}

export function redefinirSenha(token: string, senha: string): Promise<void> {
  return chamarApi<void>('POST', '/api/usuarios/redefinir-senha', { token, senha })
}
