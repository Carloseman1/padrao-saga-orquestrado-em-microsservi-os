package com.livraria.validacao.catalogo;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "livro")
@Data
@EqualsAndHashCode(of = "id")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false, unique = true)
    private String codigo;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "autor")
    private String autor;

    @Column(name = "editora")
    private String editora;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaLivro categoria;

    @Column(name = "descricao")
    private String descricao;

    @Column(name = "capa_url")
    private String capaUrl;

    protected Livro() {
    }

    public Livro(String codigo, String titulo, BigDecimal preco) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.preco = preco;
        this.categoria = CategoriaLivro.OUTROS;
    }
}