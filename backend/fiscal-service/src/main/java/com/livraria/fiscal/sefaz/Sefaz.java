package com.livraria.fiscal.sefaz;

public interface Sefaz {

    RespostaSefaz autorizar(SolicitacaoAutorizacao solicitacao);

    RespostaSefaz cancelar(String chaveAcesso, String justificativa);
}
