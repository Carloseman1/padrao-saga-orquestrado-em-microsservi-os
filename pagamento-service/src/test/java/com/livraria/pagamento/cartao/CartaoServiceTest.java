package com.livraria.pagamento.cartao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.pagamento.cartao.dto.CartaoAbertoDTO;
import com.livraria.pagamento.cartao.dto.CartaoRequest;
import com.livraria.pagamento.cartao.dto.CartaoResumoDTO;
import com.livraria.pagamento.cartao.exceptions.CartaoNaoEncontradoException;
import com.livraria.pagamento.cartao.exceptions.CartaoVencidoException;
import com.livraria.pagamento.cartao.exceptions.CvvInvalidoException;
import com.livraria.pagamento.cartao.exceptions.CvvParException;
import com.livraria.pagamento.cartao.exceptions.NumeroCartaoInvalidoException;
import com.livraria.pagamento.shared.exception.CampoObrigatorioException;

@ExtendWith(MockitoExtension.class)
class CartaoServiceTest {

    private static final String NUMERO_VALIDO = "4111 1111 1111 1111";
    private static final String NUMERO_SO_DIGITOS = "4111111111111111";
    private static final Long USUARIO = 1L;
    private static final Long OUTRO_USUARIO = 2L;
    private static final Clock SETEMBRO_DE_2026 =
            Clock.fixed(Instant.parse("2026-09-18T10:00:00Z"), ZoneId.of("UTC"));

    private final CriptografiaService criptografia =
            new CriptografiaService("chave-de-teste", "5c0744940b5c369b");

    @Mock
    private CartaoRepository cartaoRepository;

    private CartaoService cartaoService;

    @BeforeEach
    void criarService() {
        cartaoService = new CartaoService(cartaoRepository, criptografia, SETEMBRO_DE_2026);
    }

    @Test
    void cadastra_cartao_valido_e_devolve_numero_mascarado() {
        devolverOCartaoSalvo();

        CartaoResumoDTO cadastrado = cartaoService.cadastrar(requisicao(12, 2028, "123"), USUARIO);

        assertThat(cadastrado.numeroMascarado()).isEqualTo("**** **** **** 1111");
        assertThat(cadastrado.validade()).isEqualTo("12/2028");
        assertThat(cadastrado.nomeTitular()).isEqualTo("USUARIO TESTE");
    }

    @Test
    void nao_guarda_numero_nem_cvv_em_claro() {
        devolverOCartaoSalvo();

        cartaoService.cadastrar(requisicao(12, 2028, "123"), USUARIO);

        ArgumentCaptor<Cartao> capturado = ArgumentCaptor.forClass(Cartao.class);
        verify(cartaoRepository).save(capturado.capture());
        Cartao salvo = capturado.getValue();

        assertThat(salvo.getNumeroCriptografado()).isNotEqualTo(NUMERO_SO_DIGITOS);
        assertThat(salvo.getCvvCriptografado()).isNotEqualTo("123");
        assertThat(salvo.getUltimosQuatro()).isEqualTo("1111");
        assertThat(criptografia.descriptografar(salvo.getNumeroCriptografado()))
                .isEqualTo(NUMERO_SO_DIGITOS);
    }

    @Test
    void recusa_cartao_vencido_no_mes_anterior() {
        assertThatThrownBy(() -> cartaoService.cadastrar(requisicao(8, 2026, "123"), USUARIO))
                .isInstanceOf(CartaoVencidoException.class);

        verify(cartaoRepository, never()).save(any());
    }

    @Test
    void aceita_cartao_que_vence_no_mes_corrente() {
        devolverOCartaoSalvo();

        CartaoResumoDTO cadastrado = cartaoService.cadastrar(requisicao(9, 2026, "123"), USUARIO);

        assertThat(cadastrado.validade()).isEqualTo("09/2026");
    }

    @Test
    void recusa_cvv_par() {
        assertThatThrownBy(() -> cartaoService.cadastrar(requisicao(12, 2028, "124"), USUARIO))
                .isInstanceOf(CvvParException.class);

        verify(cartaoRepository, never()).save(any());
    }

    @Test
    void recusa_cvv_zerado_porque_zero_e_par() {
        assertThatThrownBy(() -> cartaoService.cadastrar(requisicao(12, 2028, "000"), USUARIO))
                .isInstanceOf(CvvParException.class);
    }

    @Test
    void recusa_cvv_fora_do_formato() {
        assertThatThrownBy(() -> cartaoService.cadastrar(requisicao(12, 2028, "12"), USUARIO))
                .isInstanceOf(CvvInvalidoException.class);
    }

    @Test
    void recusa_numero_que_falha_no_luhn() {
        CartaoRequest request =
                new CartaoRequest("4111 1111 1111 1112", "USUARIO TESTE", 12, 2028, "123");

        assertThatThrownBy(() -> cartaoService.cadastrar(request, USUARIO))
                .isInstanceOf(NumeroCartaoInvalidoException.class);
    }

    @Test
    void recusa_nome_do_titular_em_branco() {
        CartaoRequest request = new CartaoRequest(NUMERO_VALIDO, "   ", 12, 2028, "123");

        assertThatThrownBy(() -> cartaoService.cadastrar(request, USUARIO))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void cartao_do_usuario_dentro_da_validade_serve_para_pagar() {
        when(cartaoRepository.findByIdAndUsuarioId(10L, USUARIO)).thenReturn(Optional.of(umCartao()));

        assertThatCode(() -> cartaoService.exigirValidoParaPagamento(10L, USUARIO)).doesNotThrowAnyException();
    }

    @Test
    void cartao_de_outro_usuario_nao_serve_para_pagar() {
        when(cartaoRepository.findByIdAndUsuarioId(10L, OUTRO_USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.exigirValidoParaPagamento(10L, OUTRO_USUARIO))
                .isInstanceOf(CartaoNaoEncontradoException.class);
    }

    @Test
    void cartao_vencido_nao_serve_para_pagar() {
        Cartao vencido = umCartao();
        vencido.setValidadeMes(12);
        vencido.setValidadeAno(2025);
        when(cartaoRepository.findByIdAndUsuarioId(10L, USUARIO)).thenReturn(Optional.of(vencido));

        assertThatThrownBy(() -> cartaoService.exigirValidoParaPagamento(10L, USUARIO))
                .isInstanceOf(CartaoVencidoException.class);
    }

    @Test
    void listagem_devolve_numero_mascarado() {
        when(cartaoRepository.findAllByUsuarioId(USUARIO)).thenReturn(List.of(umCartao()));

        List<CartaoResumoDTO> cartoes = cartaoService.listar(USUARIO);

        assertThat(cartoes).hasSize(1);
        assertThat(cartoes.getFirst().numeroMascarado()).isEqualTo("**** **** **** 1111");
    }

    @Test
    void ler_cartao_devolve_numero_e_cvv_abertos() {
        when(cartaoRepository.findByIdAndUsuarioId(1L, USUARIO)).thenReturn(Optional.of(umCartao()));

        CartaoAbertoDTO aberto = cartaoService.lerCartao(1L, USUARIO, "usuario@teste.com");

        assertThat(aberto.numero()).isEqualTo(NUMERO_SO_DIGITOS);
        assertThat(aberto.cvv()).isEqualTo("123");
        assertThat(aberto.validade()).isEqualTo("12/2028");
    }

    @Test
    void ler_cartao_inexistente_reclama() {
        when(cartaoRepository.findByIdAndUsuarioId(99L, USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.lerCartao(99L, USUARIO, "usuario@teste.com"))
                .isInstanceOf(CartaoNaoEncontradoException.class);
    }

    @Test
    void listagem_so_traz_os_cartoes_do_proprio_usuario() {
        when(cartaoRepository.findAllByUsuarioId(USUARIO)).thenReturn(List.of(umCartao()));

        assertThat(cartaoService.listar(USUARIO)).hasSize(1);
        assertThat(cartaoService.listar(OUTRO_USUARIO)).isEmpty();
    }

    @Test
    void ler_cartao_de_outro_usuario_responde_nao_encontrado() {
        when(cartaoRepository.findByIdAndUsuarioId(1L, OUTRO_USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartaoService.lerCartao(1L, OUTRO_USUARIO, "outro@example.com"))
                .isInstanceOf(CartaoNaoEncontradoException.class);
    }

    private CartaoRequest requisicao(int validadeMes, int validadeAno, String cvv) {
        return new CartaoRequest(NUMERO_VALIDO, "USUARIO TESTE", validadeMes, validadeAno, cvv);
    }

    private Cartao umCartao() {
        NumeroCartao numero = new NumeroCartao(NUMERO_VALIDO);

        return new Cartao(
                criptografia.criptografar(numero.valor()),
                numero.ultimosQuatro(),
                criptografia.criptografar("123"),
                "USUARIO TESTE",
                new Validade(12, 2028),
                USUARIO,
                LocalDateTime.now(SETEMBRO_DE_2026));
    }

    private void devolverOCartaoSalvo() {
        when(cartaoRepository.save(any(Cartao.class))).thenAnswer(chamada -> chamada.getArgument(0));
    }
}
