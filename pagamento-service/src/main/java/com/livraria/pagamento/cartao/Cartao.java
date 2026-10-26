package com.livraria.pagamento.cartao;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "cartao")
@Data
@EqualsAndHashCode(of = "id")
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_criptografado", nullable = false)
    private String numeroCriptografado;

    @Column(name = "ultimos_quatro", nullable = false)
    private String ultimosQuatro;

    @Column(name = "cvv_criptografado", nullable = false)
    private String cvvCriptografado;

    @Column(name = "nome_titular", nullable = false)
    private String nomeTitular;

    @Column(name = "validade_mes", nullable = false)
    private Integer validadeMes;

    @Column(name = "validade_ano", nullable = false)
    private Integer validadeAno;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    protected Cartao() {
    }

    public Cartao(String numeroCriptografado, String ultimosQuatro, String cvvCriptografado,
                  String nomeTitular, Validade validade, Long usuarioId, LocalDateTime criadoEm) {
        this.numeroCriptografado = numeroCriptografado;
        this.ultimosQuatro = ultimosQuatro;
        this.cvvCriptografado = cvvCriptografado;
        this.nomeTitular = nomeTitular;
        this.validadeMes = validade.mes();
        this.validadeAno = validade.ano();
        this.usuarioId = usuarioId;
        this.criadoEm = criadoEm;
    }

    public Validade getValidade() {
        return new Validade(validadeMes, validadeAno);
    }
}
