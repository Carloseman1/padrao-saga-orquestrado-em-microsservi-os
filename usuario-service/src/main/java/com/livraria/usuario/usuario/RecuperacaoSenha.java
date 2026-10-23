package com.livraria.usuario.usuario;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "recuperacao_senha")
@Data
@EqualsAndHashCode(of = "id")
@ToString(exclude = { "usuario", "token" })
public class RecuperacaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "usado_em")
    private LocalDateTime usadoEm;

    protected RecuperacaoSenha(){}

    public RecuperacaoSenha( Usuario usuario, String token, LocalDateTime expiraEm){
        this.usuario = usuario;
        this.token = token;
        this.expiraEm = expiraEm;
    }

    public boolean estaValido(LocalDateTime agora){
        return usadoEm == null && agora.isBefore(expiraEm);
    }

    public void marcarUsada(LocalDateTime agora){
        this.usadoEm = agora;
    }
}