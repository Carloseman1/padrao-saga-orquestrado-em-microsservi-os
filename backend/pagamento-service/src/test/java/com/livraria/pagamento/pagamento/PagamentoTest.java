package com.livraria.pagamento.pagamento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.livraria.pagamento.pagamento.exceptions.ValorMinimoNaoAtingidoException;

class PagamentoTest {

    private static final LocalDateTime CRIACAO = LocalDateTime.of(2026, 10, 12, 10, 0);
    private static final LocalDateTime DEPOIS = CRIACAO.plusMinutes(1);

    @Test
    void nasce_pendente() {
        assertThat(pagamento("50.00").getStatus()).isEqualTo(StatusPagamento.PENDENTE);
    }

    @Test
    void aprova_e_atualiza_a_data() {
        Pagamento pagamento = pagamento("50.00");

        pagamento.aprovar(DEPOIS);

        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.APROVADO);
        assertThat(pagamento.getAtualizadoEm()).isEqualTo(DEPOIS);
        assertThat(pagamento.getCriadoEm()).isEqualTo(CRIACAO);
    }

    @Test
    void aceita_exatamente_o_valor_minimo() {
        Pagamento pagamento = pagamento("0.10");

        pagamento.aprovar(DEPOIS);

        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.APROVADO);
    }

    @Test
    void recusa_valor_abaixo_do_minimo() {
        Pagamento pagamento = pagamento("0.09");

        assertThatThrownBy(() -> pagamento.aprovar(DEPOIS)).isInstanceOf(ValorMinimoNaoAtingidoException.class);
        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.PENDENTE);
    }

    @Test
    void estorna() {
        Pagamento pagamento = pagamento("50.00");

        pagamento.estornar(DEPOIS);

        assertThat(pagamento.getStatus()).isEqualTo(StatusPagamento.ESTORNADO);
    }

    private Pagamento pagamento(String valor) {
        return new Pagamento("pedido-1", "transacao-1", 1, new BigDecimal(valor), CRIACAO);
    }
}
