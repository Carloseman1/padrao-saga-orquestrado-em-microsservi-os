import { useContext } from "react";
import { SessaoContexto } from "./sessaoContexto";

export function useSessao() {
    const sessao = useContext(SessaoContexto);

    if(!sessao) {
        throw new Error('useSessao precisa estar dentro do SessaoProvider')
    }

    return sessao;
}