package com.livraria.pedido.evento;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.livraria.pedido.pedido.StatusPedido;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.livraria.pedido.pedido.Pedido;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Document(collection = "evento")
@Data
@EqualsAndHashCode(of = "id")
public class Evento {

    public static final int VERSAO_ATUAL = 1;
    private static final String SUCESSO = "SUCESSO";
    private static final String ROLLBACK_PENDENTE = "ROLLBACK_PENDENTE";
    private static final String PAGAMENTO = "PAGAMENTO_SERVICE";

    @Id
    private String id;
    private Integer versao;
    private String transacaoId;
    private String pedidoId;
    private Pedido pedido;
    private String origem;
    private String status;
    private List<Historico> historico;
    private LocalDateTime criadoEm;

    protected Evento() {
    }

    public Evento(Pedido pedido, LocalDateTime criadoEm) {
        this.versao = VERSAO_ATUAL;
        this.pedidoId = pedido.getId();
        this.transacaoId = pedido.getTransacaoId();
        this.pedido = pedido;
        this.criadoEm = criadoEm;
    }

    public boolean versaoSuportada() {
        return versao != null && versao == VERSAO_ATUAL;
    }

    public void registrarFim(LocalDateTime agora) {
        this.pedidoId = pedido.getId();
        this.criadoEm = agora;
    }

    public StatusPedido resultado() {
        if (SUCESSO.equals(status)) {
            return StatusPedido.CONCLUIDO;
        }

        return primeiraFalha()
                .filter(falha -> PAGAMENTO.equals(falha.origem()))
                .map(falha -> StatusPedido.PAGAMENTO_RECUSADO)
                .orElse(StatusPedido.FALHOU);
    }

    public String motivo() {
        if (SUCESSO.equals(status)) {
            return null;
        }

        return primeiraFalha().map(Historico::mensagem).orElse(null);
    }

    private Optional<Historico> primeiraFalha() {
        if (historico == null) {
            return Optional.empty();
        }

        return historico.stream()
                .filter(registro -> ROLLBACK_PENDENTE.equals(registro.status()))
                .findFirst();
    }
}
