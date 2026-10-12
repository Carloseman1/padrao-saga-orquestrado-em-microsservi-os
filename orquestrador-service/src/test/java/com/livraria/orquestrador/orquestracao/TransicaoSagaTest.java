package com.livraria.orquestrador.orquestracao;

import static com.livraria.orquestrador.saga.OrigemEvento.ESTOQUE_SERVICE;
import static com.livraria.orquestrador.saga.OrigemEvento.ORQUESTRADOR;
import static com.livraria.orquestrador.saga.OrigemEvento.PAGAMENTO_SERVICE;
import static com.livraria.orquestrador.saga.OrigemEvento.VALIDACAO_LIVRO_SERVICE;
import static com.livraria.orquestrador.saga.StatusSaga.FALHA;
import static com.livraria.orquestrador.saga.StatusSaga.ROLLBACK_PENDENTE;
import static com.livraria.orquestrador.saga.StatusSaga.SUCESSO;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TransicaoSagaTest {

    @Test
    void caminho_feliz_passa_por_todos_os_servicos_ate_o_fim() {
        assertThat(TransicaoSaga.proximoTopico(ORQUESTRADOR, SUCESSO)).isEqualTo(Topico.VALIDACAO_LIVRO_SUCESSO);
        assertThat(TransicaoSaga.proximoTopico(VALIDACAO_LIVRO_SERVICE, SUCESSO)).isEqualTo(Topico.PAGAMENTO_SUCESSO);
        assertThat(TransicaoSaga.proximoTopico(PAGAMENTO_SERVICE, SUCESSO)).isEqualTo(Topico.ESTOQUE_SUCESSO);
        assertThat(TransicaoSaga.proximoTopico(ESTOQUE_SERVICE, SUCESSO)).isEqualTo(Topico.FIM_SUCESSO);
    }

    @Test
    void rollback_pendente_desfaz_o_proprio_servico() {
        assertThat(TransicaoSaga.proximoTopico(VALIDACAO_LIVRO_SERVICE, ROLLBACK_PENDENTE))
                .isEqualTo(Topico.VALIDACAO_LIVRO_FALHA);
        assertThat(TransicaoSaga.proximoTopico(PAGAMENTO_SERVICE, ROLLBACK_PENDENTE)).isEqualTo(Topico.PAGAMENTO_FALHA);
        assertThat(TransicaoSaga.proximoTopico(ESTOQUE_SERVICE, ROLLBACK_PENDENTE)).isEqualTo(Topico.ESTOQUE_FALHA);
    }

    @Test
    void falha_desfaz_o_servico_anterior_ate_finalizar() {
        assertThat(TransicaoSaga.proximoTopico(ESTOQUE_SERVICE, FALHA)).isEqualTo(Topico.PAGAMENTO_FALHA);
        assertThat(TransicaoSaga.proximoTopico(PAGAMENTO_SERVICE, FALHA)).isEqualTo(Topico.VALIDACAO_LIVRO_FALHA);
        assertThat(TransicaoSaga.proximoTopico(VALIDACAO_LIVRO_SERVICE, FALHA)).isEqualTo(Topico.FIM_FALHA);
        assertThat(TransicaoSaga.proximoTopico(ORQUESTRADOR, FALHA)).isEqualTo(Topico.FIM_FALHA);
    }
}
