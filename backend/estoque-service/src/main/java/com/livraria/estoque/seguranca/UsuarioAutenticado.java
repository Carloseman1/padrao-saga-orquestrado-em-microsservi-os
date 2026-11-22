package com.livraria.estoque.seguranca;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.livraria.estoque.seguranca.exceptions.AcessoNegadoException;

@Component
public class UsuarioAutenticado {

    public Long id() {
        return (Long) atributo(JwtAuthFilter.ATRIBUTO_ID);
    }

    public String email() {
        return atributo(JwtAuthFilter.ATRIBUTO_EMAIL).toString();
    }

    public String perfil() {
        return atributo(JwtAuthFilter.ATRIBUTO_PERFIL).toString();
    }

    public void exigirPerfil(String... permitidos) {
        if (!List.of(permitidos).contains(perfil())) {
            throw new AcessoNegadoException();
        }
    }

    private Object atributo(String nome) {
        ServletRequestAttributes atributos =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        Object valor = atributos == null ? null : atributos.getRequest().getAttribute(nome);

        if (valor == null) {
            throw new IllegalStateException(
                    "Rota protegida sem " + nome + " na requisicao: o JwtAuthFilter nao passou por aqui.");
        }

        return valor;
    }
}