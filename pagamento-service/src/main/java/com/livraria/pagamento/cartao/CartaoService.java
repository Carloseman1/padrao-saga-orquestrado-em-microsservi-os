package com.livraria.pagamento.cartao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.livraria.pagamento.cartao.dto.CartaoAbertoDTO;
import com.livraria.pagamento.cartao.dto.CartaoRequest;
import com.livraria.pagamento.cartao.dto.CartaoResumoDTO;
import com.livraria.pagamento.cartao.exceptions.CartaoNaoEncontradoException;
import com.livraria.pagamento.cartao.exceptions.CartaoVencidoException;
import com.livraria.pagamento.cartao.exceptions.CvvParException;
import com.livraria.pagamento.shared.exception.CampoObrigatorioException;

@Service
public class CartaoService {

    private static final Logger log = LoggerFactory.getLogger(CartaoService.class);

    private final CartaoRepository cartaoRepository;
    private final CriptografiaService criptografia;
    private final Clock clock;

    public CartaoService(CartaoRepository cartaoRepository, CriptografiaService criptografia,
                         Clock clock) {
        this.cartaoRepository = cartaoRepository;
        this.criptografia = criptografia;
        this.clock = clock;
    }

    public CartaoResumoDTO cadastrar(CartaoRequest request, Long usuarioId) {
        NumeroCartao numero = new NumeroCartao(request.numero());
        Cvv cvv = new Cvv(request.cvv());
        Validade validade = new Validade(request.validadeMes(), request.validadeAno());
        String nomeTitular = nomeTitularValidado(request.nomeTitular());

        if (validade.estaVencida(YearMonth.now(clock))) {
            throw new CartaoVencidoException();
        }

        if (cvv.ehPar()) {
            throw new CvvParException();
        }

        Cartao cartao = new Cartao(
                criptografia.criptografar(numero.valor()),
                numero.ultimosQuatro(),
                criptografia.criptografar(cvv.valor()),
                nomeTitular,
                validade,
                usuarioId,
                LocalDateTime.now(clock));

        return CartaoResumoDTO.de(cartaoRepository.save(cartao));
    }

    public List<CartaoResumoDTO> listar(Long usuarioId) {
        return cartaoRepository.findAllByUsuarioId(usuarioId).stream()
                .map(CartaoResumoDTO::de)
                .toList();
    }

    public CartaoAbertoDTO lerCartao(Long id, Long usuarioId, String lidoPor) {
        Cartao cartao = cartaoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(CartaoNaoEncontradoException::new);

        log.info("Leitura dos dados abertos do cartao {} por {}", id, lidoPor);

        return new CartaoAbertoDTO(
                cartao.getId(),
                criptografia.descriptografar(cartao.getNumeroCriptografado()),
                cartao.getNomeTitular(),
                cartao.getValidade().formatada(),
                criptografia.descriptografar(cartao.getCvvCriptografado()));
    }

    private String nomeTitularValidado(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new CampoObrigatorioException("nome do titular");
        }
        return nome.trim();
    }

    public void exigirValidoParaPagamento(Long cartaoId, Long usuarioId){
        Cartao cartao = cartaoRepository.findByIdAndUsuarioId(cartaoId,usuarioId)
                .orElseThrow(CartaoNaoEncontradoException::new);

        if(cartao.getValidade().estaVencida(YearMonth.now(clock))){
            throw new CartaoVencidoException();
        }
    }


}
