package com.livraria.pedido.seguranca;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UsuarioAutenticado {

    public Long id(){
        return (Long) atributo(JwtAuthFilter.ATRIBUTO_ID);
    }

    public String email(){
        return atributo(JwtAuthFilter.ATRIBUTO_EMAIL).toString();
    }

    private Object atributo(String nome){
        ServletRequestAttributes atributos = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        Object valor = atributos == null ? null : atributos.getRequest().getAttribute(nome);

        if(valor == null){
            throw new IllegalStateException(
                    "Rota protegida sem " + nome + " na requisição: O JwtAuthFileter nao passou por aqui.");
        }

        return valor;
    }
}
