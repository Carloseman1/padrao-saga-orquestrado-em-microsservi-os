package com.livraria.orquestrador.orquestracao;

import static com.livraria.orquestrador.saga.OrigemEvento.ESTOQUE_SERVICE;
import static com.livraria.orquestrador.saga.OrigemEvento.ORQUESTRADOR;
import static com.livraria.orquestrador.saga.OrigemEvento.PAGAMENTO_SERVICE;
import static com.livraria.orquestrador.saga.OrigemEvento.VALIDACAO_LIVRO_SERVICE;
import static com.livraria.orquestrador.saga.StatusSaga.FALHA;
import static com.livraria.orquestrador.saga.StatusSaga.ROLLBACK_PENDENTE;
import static com.livraria.orquestrador.saga.StatusSaga.SUCESSO;

import java.util.Arrays;

import com.livraria.orquestrador.orquestracao.exceptions.TransicaoNaoEncontradaException;
import com.livraria.orquestrador.saga.OrigemEvento;
import com.livraria.orquestrador.saga.StatusSaga;

public enum TransicaoSaga {

    ORQUESTRADOR_SUCESSO(ORQUESTRADOR, SUCESSO, Topico.VALIDACAO_LIVRO_SUCESSO),
    ORQUESTRADOR_FALHA(ORQUESTRADOR, FALHA, Topico.FIM_FALHA),

    VALIDACAO_LIVRO_ROLLBACK(VALIDACAO_LIVRO_SERVICE, ROLLBACK_PENDENTE, Topico.VALIDACAO_LIVRO_FALHA),
    VALIDACAO_LIVRO_FALHA(VALIDACAO_LIVRO_SERVICE, FALHA, Topico.FIM_FALHA),
    VALIDACAO_LIVRO_SUCESSO(VALIDACAO_LIVRO_SERVICE, SUCESSO, Topico.PAGAMENTO_SUCESSO),

    PAGAMENTO_ROLLBACK(PAGAMENTO_SERVICE, ROLLBACK_PENDENTE, Topico.PAGAMENTO_FALHA),
    PAGAMENTO_FALHA(PAGAMENTO_SERVICE, FALHA, Topico.VALIDACAO_LIVRO_FALHA),
    PAGAMENTO_SUCESSO(PAGAMENTO_SERVICE, SUCESSO, Topico.ESTOQUE_SUCESSO),

    ESTOQUE_ROLLBACK(ESTOQUE_SERVICE, ROLLBACK_PENDENTE, Topico.ESTOQUE_FALHA),
    ESTOQUE_FALHA(ESTOQUE_SERVICE, FALHA, Topico.PAGAMENTO_FALHA),
    ESTOQUE_SUCESSO(ESTOQUE_SERVICE, SUCESSO, Topico.FIM_SUCESSO);

    private final OrigemEvento origem;
    private final StatusSaga status;
    private final Topico proximoTopico;

    TransicaoSaga(OrigemEvento origem, StatusSaga status, Topico proximoTopico) {
        this.origem = origem;
        this.status = status;
        this.proximoTopico = proximoTopico;
    }

    public static Topico proximoTopico(OrigemEvento origem, StatusSaga status) {
        return Arrays.stream(values())
                .filter(transicao -> transicao.origem == origem && transicao.status == status)
                .map(transicao -> transicao.proximoTopico)
                .findFirst()
                .orElseThrow(TransicaoNaoEncontradaException::new);
    }
}
