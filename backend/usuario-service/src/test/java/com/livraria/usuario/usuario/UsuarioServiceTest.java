package com.livraria.usuario.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.livraria.usuario.endereco.Endereco;
import com.livraria.usuario.endereco.EnderecoService;
import com.livraria.usuario.seguranca.JwtService;
import com.livraria.usuario.shared.exception.CampoObrigatorioException;
import com.livraria.usuario.usuario.dto.LoginRequest;
import com.livraria.usuario.usuario.dto.LoginResponseDTO;
import com.livraria.usuario.usuario.dto.PerfilRequest;
import com.livraria.usuario.usuario.dto.RecuperacaoRequest;
import com.livraria.usuario.usuario.dto.RedefinicaoRequest;
import com.livraria.usuario.usuario.dto.UsuarioDTO;
import com.livraria.usuario.usuario.dto.UsuarioRequest;
import com.livraria.usuario.usuario.exceptions.CredenciaisInvalidasException;
import com.livraria.usuario.usuario.exceptions.EmailCadastradoException;
import com.livraria.usuario.usuario.exceptions.EmailInvalidoException;
import com.livraria.usuario.usuario.exceptions.MuitasTentativasException;
import com.livraria.usuario.usuario.exceptions.SenhaForaDoPadraoException;
import com.livraria.usuario.usuario.exceptions.TokenRecuperacaoInvalidoException;
import com.livraria.usuario.usuario.exceptions.UsuarioNaoEncontradoException;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final String EMAIL = "usuario@teste.com";
    private static final String SENHA = "Senha@123";
    private static final String SENHA_NOVA = "Nova@1234";
    private static final String TOKEN = "token-bom";

    private static final Clock RELOGIO =
            Clock.fixed(Instant.parse("2026-09-18T10:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RecuperacaoSenhaRepository recuperacaoRepository;

    @Mock
    private EnderecoService enderecoService;

    @Mock
    private JwtService jwtService;

    private UsuarioService usuarioService;

    @BeforeEach
    void criarService() {
        usuarioService = new UsuarioService(usuarioRepository, recuperacaoRepository,
                enderecoService, jwtService, new ControleDeTentativas(60, RELOGIO), RELOGIO, 30);
    }

    @Test
    void cadastra_completando_o_endereco_do_viacep_com_numero_e_complemento() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(enderecoService.buscar("01001-000")).thenReturn(enderecoDoCep());
        devolverOUsuarioSalvo();

        UsuarioDTO cadastrado = usuarioService.cadastrar(requisicao());

        assertThat(cadastrado.email()).isEqualTo(EMAIL);
        assertThat(cadastrado.endereco().cidade()).isEqualTo("Sao Paulo");
        assertThat(cadastrado.endereco().numero()).isEqualTo("100");
        assertThat(cadastrado.endereco().complemento()).isEqualTo("apto 42");
    }

    @Test
    void nao_guarda_a_senha_em_claro() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(enderecoService.buscar("01001-000")).thenReturn(enderecoDoCep());
        devolverOUsuarioSalvo();

        usuarioService.cadastrar(requisicao());

        ArgumentCaptor<Usuario> capturado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(capturado.capture());

        assertThat(capturado.getValue().getSenha()).isNotEqualTo(SENHA).startsWith("$2");
    }

    @Test
    void email_repetido_nao_gasta_chamada_ao_viacep() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(umUsuarioSalvo()));

        assertThatThrownBy(() -> usuarioService.cadastrar(requisicao()))
                .isInstanceOf(EmailCadastradoException.class);

        verifyNoInteractions(enderecoService);
    }

    @Test
    void senha_fora_do_padrao_para_antes_de_tudo() {
        UsuarioRequest request = new UsuarioRequest(
                "Usuario", "Teste", EMAIL, "123456", "01001-000", "100", null);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(SenhaForaDoPadraoException.class);

        verifyNoInteractions(usuarioRepository, enderecoService);
    }

    @Test
    void email_invalido_para_antes_de_tudo() {
        UsuarioRequest request = new UsuarioRequest(
                "Usuario", "Teste", "usuario", SENHA, "01001-000", "100", null);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(EmailInvalidoException.class);

        verifyNoInteractions(usuarioRepository, enderecoService);
    }

    @Test
    void numero_do_endereco_em_branco_reclama() {
        UsuarioRequest request = new UsuarioRequest(
                "Usuario", "Teste", EMAIL, SENHA, "01001-000", "  ", null);

        assertThatThrownBy(() -> usuarioService.cadastrar(request))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void login_valido_devolve_o_token_e_o_nome_completo() {
        Usuario usuario = umUsuarioSalvo();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(jwtService.gerarToken(EMAIL, usuario.getId(), "CLIENTE")).thenReturn("token-de-teste");

        LoginResponseDTO resposta = usuarioService.login(
                new LoginRequest("USUARIO@TESTE.COM", SENHA));

        assertThat(resposta.token()).isEqualTo("token-de-teste");
        assertThat(resposta.nome()).isEqualTo("Usuario Teste");
        assertThat(resposta.email()).isEqualTo(EMAIL);
    }

    @Test
    void login_com_senha_errada_recusa() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(umUsuarioSalvo()));

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest(EMAIL, "Outra@123")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void login_com_email_inexistente_recusa() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest(EMAIL, SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void login_com_email_fora_do_formato_nao_revela_o_motivo() {
        assertThatThrownBy(() -> usuarioService.login(new LoginRequest("usuario", SENHA)))
                .isInstanceOf(CredenciaisInvalidasException.class);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void cinco_senhas_erradas_bloqueiam_o_login() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(umUsuarioSalvo()));

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertThatThrownBy(() -> usuarioService.login(new LoginRequest(EMAIL, "Errada@123")))
                    .isInstanceOf(CredenciaisInvalidasException.class);
        }

        assertThatThrownBy(() -> usuarioService.login(new LoginRequest(EMAIL, SENHA)))
                .isInstanceOf(MuitasTentativasException.class);
    }

    @Test
    void pedido_de_recuperacao_para_email_inexistente_nao_grava_nada() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        usuarioService.solicitarRecuperacao(new RecuperacaoRequest(EMAIL));

        verifyNoInteractions(recuperacaoRepository);
    }

    @Test
    void pedido_de_recuperacao_apaga_os_anteriores_e_grava_um_token() {
        Usuario usuario = umUsuarioSalvo();
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        usuarioService.solicitarRecuperacao(new RecuperacaoRequest(EMAIL));

        verify(recuperacaoRepository).deleteByUsuarioIdAndUsadoEmIsNull(usuario.getId());

        ArgumentCaptor<RecuperacaoSenha> capturado =
                ArgumentCaptor.forClass(RecuperacaoSenha.class);
        verify(recuperacaoRepository).save(capturado.capture());

        assertThat(capturado.getValue().getToken()).isNotBlank();
        assertThat(capturado.getValue().estaValida(agora())).isTrue();
    }

    @Test
    void redefinir_com_token_valido_troca_a_senha() {
        Usuario usuario = umUsuarioSalvo();
        when(recuperacaoRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(umaRecuperacaoValida(usuario)));

        usuarioService.redefinirSenha(new RedefinicaoRequest(TOKEN, SENHA_NOVA));

        assertThat(new BCryptPasswordEncoder().matches(SENHA_NOVA, usuario.getSenha())).isTrue();
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void token_usado_uma_vez_nao_serve_de_novo() {
        RecuperacaoSenha recuperacao = umaRecuperacaoValida(umUsuarioSalvo());
        when(recuperacaoRepository.findByToken(TOKEN)).thenReturn(Optional.of(recuperacao));

        usuarioService.redefinirSenha(new RedefinicaoRequest(TOKEN, SENHA_NOVA));

        assertThat(recuperacao.estaValida(agora())).isFalse();
    }

    @Test
    void redefinir_com_token_desconhecido_recusa() {
        when(recuperacaoRepository.findByToken("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.redefinirSenha(
                new RedefinicaoRequest("nao-existe", SENHA_NOVA)))
                .isInstanceOf(TokenRecuperacaoInvalidoException.class);
    }

    @Test
    void redefinir_com_token_expirado_recusa() {
        RecuperacaoSenha expirada = new RecuperacaoSenha(
                umUsuarioSalvo(), "token-velho", agora().minusMinutes(1));
        when(recuperacaoRepository.findByToken("token-velho")).thenReturn(Optional.of(expirada));

        assertThatThrownBy(() -> usuarioService.redefinirSenha(
                new RedefinicaoRequest("token-velho", SENHA_NOVA)))
                .isInstanceOf(TokenRecuperacaoInvalidoException.class);
    }

    @Test
    void redefinir_com_senha_fraca_recusa_e_nao_grava() {
        when(recuperacaoRepository.findByToken(TOKEN))
                .thenReturn(Optional.of(umaRecuperacaoValida(umUsuarioSalvo())));

        assertThatThrownBy(() -> usuarioService.redefinirSenha(
                new RedefinicaoRequest(TOKEN, "123456")))
                .isInstanceOf(SenhaForaDoPadraoException.class);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void cadastro_novo_nasce_como_cliente() {
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(enderecoService.buscar("01001-000")).thenReturn(enderecoDoCep());
        devolverOUsuarioSalvo();

        UsuarioDTO cadastrado = usuarioService.cadastrar(requisicao());

        assertThat(cadastrado.perfil()).isEqualTo(Perfil.CLIENTE);
    }

    @Test
    void admin_promove_cliente_a_operador() {
        Usuario usuario = umUsuarioSalvo();
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(usuario));
        devolverOUsuarioSalvo();

        UsuarioDTO alterado = usuarioService.alterarPerfil(7L, new PerfilRequest(Perfil.OPERADOR));

        assertThat(alterado.perfil()).isEqualTo(Perfil.OPERADOR);
        assertThat(usuario.getPerfil()).isEqualTo(Perfil.OPERADOR);
    }

    @Test
    void alterar_perfil_de_usuario_inexistente_reclama() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.alterarPerfil(99L, new PerfilRequest(Perfil.ADMIN)))
                .isInstanceOf(UsuarioNaoEncontradoException.class);
    }

    @Test
    void alterar_perfil_sem_informar_o_perfil_reclama() {
        assertThatThrownBy(() -> usuarioService.alterarPerfil(7L, new PerfilRequest(null)))
                .isInstanceOf(CampoObrigatorioException.class);

        verifyNoInteractions(usuarioRepository);
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(RELOGIO);
    }

    private RecuperacaoSenha umaRecuperacaoValida(Usuario usuario) {
        return new RecuperacaoSenha(usuario, TOKEN, agora().plusMinutes(30));
    }

    private UsuarioRequest requisicao() {
        return new UsuarioRequest("Usuario", "Teste", EMAIL, SENHA, "01001-000", "100", "apto 42");
    }

    private Endereco enderecoDoCep() {
        return Endereco.doCep("01001000", "Praca da Se", "Se", "Sao Paulo", "SP");
    }

    private Usuario umUsuarioSalvo() {
        return new Usuario("Usuario", "Teste", EMAIL,
                new BCryptPasswordEncoder().encode(SENHA),
                enderecoDoCep().completadoCom("100", null));
    }

    private void devolverOUsuarioSalvo() {
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(chamada -> chamada.getArgument(0));
    }
}