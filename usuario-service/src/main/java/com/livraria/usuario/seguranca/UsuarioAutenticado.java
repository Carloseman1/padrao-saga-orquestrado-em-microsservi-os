package com.livraria.usuario.seguranca;


import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UsuarioAutenticado {

    public long id() {
        return (Long) atributo(JwtAuthFilter.ATRIBUTO_ID);
    }

    public String email() {
        return atributo(JwtAuthFilter.ATRIBUTO_EMAIL).toString();
    }

    public String token(){
        return atributo(JwtAuthFilter.ATRIBUTO_TOKEN).toString();
    }

    public Object atributo(String nome){
        ServletRequestAttributes atributos =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        Object valor = atributos == null ? null : atributos.getRequest().getAttribute(nome);

        if(valor == null){
            throw new IllegalStateException( "Rota protegida sem " + nome + " na requisicao: o JwtAuthFielter nao " +
                    "passsou por aqui.");
        }

        return valor;
    }
}
