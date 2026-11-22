package com.livraria.usuario.usuario;

import com.livraria.usuario.endereco.Endereco;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "usuario")
@Data
@EqualsAndHashCode(of="id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String sobrenome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private boolean ativo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column(nullable = false)
    private String cep;

    private String rua;

    private String bairro;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String numero;

    private String complemento;

    protected Usuario() {
    }

    public Usuario(String nome, String sobrenome, String email, String senha, Endereco
                   endereco){
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
        this.senha = senha;
        this.ativo = true;
        this.perfil = Perfil.CLIENTE;
        this.cep = endereco.cep();
        this.rua = endereco.rua();
        this.bairro = endereco.bairro();
        this.cidade = endereco.cidade();
        this.estado = endereco.estado();
        this.numero = endereco.numero();
        this.complemento = endereco.complemento();
    }

    public Endereco getEndereco(){
        return new Endereco(cep, rua, bairro, cidade, estado, numero, complemento);
    }

    public String getNomeCompleto(){
        return nome + " " + sobrenome;
    }
}
