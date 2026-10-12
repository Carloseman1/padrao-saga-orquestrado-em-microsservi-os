package com.livraria.orquestrador.orquestracao;

public enum Topico {

    INICIO_SAGA("inicio-saga"),
    ORQUESTRADOR("orquestrador"),
    FIM_SUCESSO("fim-sucesso"),
    FIM_FALHA("fim-falha"),
    VALIDACAO_LIVRO_SUCESSO("validacao-livro-sucesso"),
    VALIDACAO_LIVRO_FALHA("validacao-livro-falha"),
    PAGAMENTO_SUCESSO("pagamento-sucesso"),
    PAGAMENTO_FALHA("pagamento-falha"),
    ESTOQUE_SUCESSO("estoque-sucesso"),
    ESTOQUE_FALHA("estoque-falha"),
    NOTIFICAR_FIM("notificar-fim");

    private final String nome;

    Topico(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
