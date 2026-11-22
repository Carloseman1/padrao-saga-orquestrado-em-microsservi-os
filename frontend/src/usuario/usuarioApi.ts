import { chamarApi } from "../compartilhado/https";

export type Usuario = {
    id: number;
    nome: string;
    email: string;
}

export function entrar(email: string, senha: string): Promise<Usuario> {
    return chamarApi<Usuario>('POST', 'api/usuario/login', { email, senha });
}

export function sair(): Promise<void> {
    return chamarApi<void>('DELETE', '/api/sessao');
}