package com.livraria.fiscal.fiscal;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.livraria.fiscal.fiscal.exceptions.NotaFiscalDuplicadaException;
import com.livraria.fiscal.saga.Evento;
import com.livraria.fiscal.saga.OrquestradorProducer;
import com.livraria.fiscal.saga.Pedido;
import com.livraria.fiscal.saga.StatusSaga;
import com.livraria.fiscal.sefaz.RespostaSefaz;
import com.livraria.fiscal.sefaz.Sefaz;
import com.livraria.fiscal.sefaz.SolicitacaoAutorizacao;

@Service
public class FiscalService {

    private static final Logger log = LoggerFactory.getLogger(FiscalService.class);
    private static final String ORIGEM = "FISCAL_SERVICE";
    private static final String JUSTIFICATIVA_CANCELAMENTO = "Pedido cancelado pela saga antes da entrega";

    private final NotaFiscalRepository notaFiscalRepository;
    private final GeradorDeNotaFiscal geradorDeNotaFiscal;
    private final Sefaz sefaz;
    private final OrquestradorProducer orquestradorProducer;
    private final TransactionTemplate transacao;
    private final Clock clock;

    public FiscalService(NotaFiscalRepository notaFiscalRepository, GeradorDeNotaFiscal geradorDeNotaFiscal,
                         Sefaz sefaz, OrquestradorProducer orquestradorProducer, TransactionTemplate transacao,
                         Clock clock) {
        this.notaFiscalRepository = notaFiscalRepository;
        this.geradorDeNotaFiscal = geradorDeNotaFiscal;
        this.sefaz = sefaz;
        this.orquestradorProducer = orquestradorProducer;
        this.transacao = transacao;
        this.clock = clock;
    }

    public void emitir(Evento evento) {
        try {
            exigirNaoEmitida(evento);

            Pedido pedido = evento.getPedido();
            NotaFiscal nota = geradorDeNotaFiscal.novaNota(
                    pedido.getId(), evento.getTransacaoId(), pedido.calcularValorTotal());
            RespostaSefaz resposta = sefaz.autorizar(new SolicitacaoAutorizacao(
                    nota.getChaveAcesso(), nota.getValorTotal(), codigosDosLivros(pedido)));

            if (resposta.aceita()) {
                nota.autorizar(resposta.protocolo(), agora());
                evento.registrar(ORIGEM, StatusSaga.SUCESSO,
                        "NF-e autorizada com a chave de acesso " + nota.getChaveAcesso() + ".", agora());
            } else {
                nota.rejeitar(resposta.codigo(), resposta.motivo(), agora());
                evento.registrar(ORIGEM, StatusSaga.ROLLBACK_PENDENTE,
                        "NF-e rejeitada pela SEFAZ: " + resposta.codigo() + " - " + resposta.motivo() + ".", agora());
            }

            gravarComResposta(nota, evento);
        } catch (NotaFiscalDuplicadaException ex) {
            log.warn("Mensagem duplicada ignorada: a NF-e do pedido {} na transação {} já foi emitida",
                    evento.getPedido().getId(), evento.getTransacaoId());
        }
    }

    public void cancelar(Evento evento) {
        NotaFiscal nota = notaFiscalRepository
                .findByPedidoIdAndTransacaoId(evento.getPedido().getId(), evento.getTransacaoId())
                .orElse(null);

        if (nota == null || !nota.foiAutorizada()) {
            String mensagem = nota != null && nota.foiCancelada()
                    ? "NF-e já estava cancelada."
                    : "Nenhuma NF-e autorizada, não há o que cancelar.";
            evento.registrar(ORIGEM, StatusSaga.FALHA, mensagem, agora());
            orquestradorProducer.enviar(evento);
            return;
        }

        RespostaSefaz resposta = sefaz.cancelar(nota.getChaveAcesso(), JUSTIFICATIVA_CANCELAMENTO);

        if (!resposta.aceita()) {
            evento.registrar(ORIGEM, StatusSaga.FALHA,
                    "Cancelamento da NF-e recusado pela SEFAZ: " + resposta.codigo() + " - " + resposta.motivo() + ".",
                    agora());
            orquestradorProducer.enviar(evento);
            return;
        }

        nota.cancelar(resposta.protocolo(), agora());
        evento.registrar(ORIGEM, StatusSaga.FALHA, "NF-e cancelada com o evento 110111.", agora());
        gravarComResposta(nota, evento);
    }

    private void exigirNaoEmitida(Evento evento) {
        if (notaFiscalRepository.existsByPedidoIdAndTransacaoId(evento.getPedido().getId(), evento.getTransacaoId())) {
            throw new NotaFiscalDuplicadaException();
        }
    }

    private List<String> codigosDosLivros(Pedido pedido) {
        return pedido.getItens().stream()
                .map(item -> item.livro().codigo())
                .toList();
    }

    private void gravarComResposta(NotaFiscal nota, Evento evento) {
        try {
            transacao.executeWithoutResult(status -> {
                notaFiscalRepository.save(nota);
                orquestradorProducer.enviar(evento);
            });
        } catch (DataIntegrityViolationException ex) {
            throw new NotaFiscalDuplicadaException();
        }
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
