package com.livraria.estoque.estoque;

import com.livraria.estoque.estoque.exceptions.EstoqueInsuficienteException;
import com.livraria.estoque.estoque.exceptions.QuantidadeInvalidaException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "estoque")
@Data
@EqualsAndHashCode(of = "id")
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_livro", nullable = false, unique = true)
    private String codigoLivro;

    @Column(name = "disponivel", nullable = false)
    private int disponivel;


    protected Estoque() {
    }

    public Estoque(String codigoLivro, int disponivel) {
        this.codigoLivro = codigoLivro;
        this.disponivel = disponivel;
    }

    public void baixar(int quantidade) {
        if (quantidade > disponivel) {
            throw new EstoqueInsuficienteException();
        }
        this.disponivel -= quantidade;
    }

    public void restaurar(int quantidadeAnterior) {
        this.disponivel = quantidadeAnterior;
    }

    public void repor(int quantidade) {
        if (quantidade <= 0) {
            throw new QuantidadeInvalidaException();
        }
        this.disponivel += quantidade;
    }
}
