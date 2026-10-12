package com.livraria.orquestrador.orquestracao;

import java.time.Clock;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.livraria.orquestrador.orquestracao.exceptions.OrigemOuStatusNaoInformadoException;
import com.livraria.orquestrador.saga.Evento;
import com.livraria.orquestrador.saga.OrigemEvento;
import com.livraria.orquestrador.saga.SagaProducer;
import com.livraria.orquestrador.saga.StatusSaga;

@Service
public class OrquestracaoService {

    private static final Logger log = LoggerFactory.getLogger(OrquestracaoService.class);

    private final SagaProducer sagaProducer;
    private final Clock clock;

    public OrquestracaoService(SagaProducer sagaProducer, Clock clock) {
        this.sagaProducer = sagaProducer;
        this.clock = clock;
    }

    public void iniciar(Evento evento) {
        evento.registrar(OrigemEvento.ORQUESTRADOR, StatusSaga.SUCESSO, "Saga iniciada.", agora());
        enviarParaProximoTopico(evento);
    }

    public void continuar(Evento evento) {
        enviarParaProximoTopico(evento);
    }

    public void finalizarComSucesso(Evento evento) {
        evento.registrar(OrigemEvento.ORQUESTRADOR, StatusSaga.SUCESSO, "Saga finalizada com sucesso.", agora());
        log.info("Saga finalizada com sucesso para o evento {}", evento.getId());
        sagaProducer.enviar(evento, Topico.NOTIFICAR_FIM);
    }

    public void finalizarComFalha(Evento evento) {
        evento.registrar(OrigemEvento.ORQUESTRADOR, StatusSaga.FALHA, "Saga finalizada com erros.", agora());
        log.info("Saga finalizada com erros para o evento {}", evento.getId());
        sagaProducer.enviar(evento, Topico.NOTIFICAR_FIM);
    }

    private void enviarParaProximoTopico(Evento evento) {
        if (evento.getOrigem() == null || evento.getStatus() == null) {
            throw new OrigemOuStatusNaoInformadoException();
        }

        Topico proximo = TransicaoSaga.proximoTopico(evento.getOrigem(), evento.getStatus());
        log.info("Saga da transacao {} | origem {} | status {} | proximo topico {}",
                evento.getTransacaoId(), evento.getOrigem(), evento.getStatus(), proximo.getNome());
        sagaProducer.enviar(evento, proximo);
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
