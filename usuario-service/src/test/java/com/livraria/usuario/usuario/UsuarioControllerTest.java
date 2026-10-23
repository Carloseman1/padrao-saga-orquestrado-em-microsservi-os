package com.livraria.usuario.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.usuario.seguranca.CookieDeSessao;
import com.livraria.usuario.usuario.dto.LoginRequest;
import com.livraria.usuario.usuario.dto.LoginResponseDTO;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private static final String TOKEN = "token-de-teste";

    @Mock
    private UsuarioService usuarioService;

    @Test
    void login_grava_o_token_no_cookie_e_nao_no_corpo_da_resposta() throws Exception {
        LoginRequest request = new LoginRequest("usuario@teste.com", "Senha@123");
        when(usuarioService.login(request))
                .thenReturn(new LoginResponseDTO(7L, "Usuario Teste", "usuario@teste.com", TOKEN));
        UsuarioController controller = new UsuarioController(
                usuarioService, new CookieDeSessao("livraria_sessao", false, 3_600_000L));

        ResponseEntity<LoginResponseDTO> resposta = controller.login(request);

        assertThat(resposta.getHeaders().getFirst(HttpHeaders.SET_COOKIE))
                .startsWith("livraria_sessao=" + TOKEN)
                .contains("HttpOnly");
        assertThat(new ObjectMapper().writeValueAsString(resposta.getBody()))
                .contains("Usuario Teste")
                .doesNotContain(TOKEN);
    }
}