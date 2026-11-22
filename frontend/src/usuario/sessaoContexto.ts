import { createContext } from "react";
import type { Usuario } from "./usuarioApi";

export type Sessao = {
    usuario: Usuario | null;
    entrar: (email: string, senha: string) => Promise<void>;
    sair: () => Promise<void>;
}

export const SessaoContexto = createContext<Sessao | null>(null);