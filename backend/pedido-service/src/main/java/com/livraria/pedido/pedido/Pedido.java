package com.livraria.pedido.pedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.livraria.pedido.pedido.exceptions.CartaoNaoInformadoException;
import com.livraria.pedido.pedido.exceptions.TentativaNaoPermitidaException;
import lombok.AccessLevel;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import com.livraria.pedido.pedido.exceptions.PedidoSemItensException;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Document(collection = "pedido")
@Data
@EqualsAndHashCode(of = "id")
public class Pedido {

    @Id
    private String id;
    private List<ItemPedido> itens;
    private Long usuarioId;
    private Long cartaoId;
    private String transacaoId;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal valorTotal;
    private int quantidadeTotal;
    private LocalDateTime criadoEm;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private StatusPedido status;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private String motivo;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private List<Tentativa> tentativas;

    @Version
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    private Long versao;

    protected Pedido() {
    }

    public Pedido(List<ItemPedido> itens, Long usuarioId,Long cartaoId, String transacaoId, LocalDateTime criadoEm) {
        if (itens == null || itens.isEmpty()) {
            throw new PedidoSemItensException();
        }

        if(cartaoId == null){
            throw new CartaoNaoInformadoException();
        }

        this.itens = itens;
        this.usuarioId = usuarioId;
        this.cartaoId = cartaoId;
        this.transacaoId = transacaoId;
        this.valorTotal = BigDecimal.ZERO;
        this.criadoEm = criadoEm;
        this.status = StatusPedido.PROCESSANDO;
        this.tentativas = new ArrayList<>();
    }

    public boolean finalizar(String transacaoDaSaga, StatusPedido resultado, String motivo,
                             LocalDateTime agora){
        if(!transacaoId.equals(transacaoDaSaga) || status != StatusPedido.PROCESSANDO){
            return  false;
        }

        this.status = resultado;
        this.motivo = motivo;
        this.tentativas.add(new Tentativa(transacaoId, cartaoId, resultado, motivo, agora));
        return true;
    }

    public void novaTentativa(Long novoCartaoId, String novaTransacaoId){
        if(status != StatusPedido.PAGAMENTO_RECUSADO){
            throw new TentativaNaoPermitidaException();
        }

        if(novoCartaoId == null){
            throw new CartaoNaoInformadoException();
        }

        this.cartaoId = novoCartaoId;
        this.transacaoId = novaTransacaoId;
        this.status = StatusPedido.PROCESSANDO;
        this.motivo = null;
    }
}
