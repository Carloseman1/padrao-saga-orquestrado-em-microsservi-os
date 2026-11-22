package com.livraria.validacao.seguranca;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.livraria.validacao.seguranca.exceptions.AcessoNegadoException;

class UsuarioAutenticadoTest {

    private final UsuarioAutenticado usuarioAutenticado = new UsuarioAutenticado();

    @AfterEach
    void limparRequisicao() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void perfil_permitido_passa() {
        logadoComo("ADMIN");

        assertThatCode(() -> usuarioAutenticado.exigirPerfil(Perfis.ADMIN)).doesNotThrowAnyException();
    }

    @Test
    void basta_ter_um_dos_perfis_permitidos() {
        logadoComo("OPERADOR");

        assertThatCode(() -> usuarioAutenticado.exigirPerfil(Perfis.OPERADOR, Perfis.ADMIN)).doesNotThrowAnyException();
    }

    @Test
    void perfil_sem_permissao_e_recusado() {
        logadoComo("CLIENTE");

        assertThatThrownBy(() -> usuarioAutenticado.exigirPerfil(Perfis.OPERADOR, Perfis.ADMIN))
                .isInstanceOf(AcessoNegadoException.class);
    }

    private void logadoComo(String perfil) {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.setAttribute(JwtAuthFilter.ATRIBUTO_PERFIL, perfil);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(requisicao));
    }
}
