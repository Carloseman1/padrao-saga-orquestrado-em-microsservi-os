package com.livraria.validacao.validacao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.livraria.validacao.saga.ConversorJson;

@Component
public class ValidacaoConsumer {

    private static final Logger log = LoggerFactory.getLogger(ValidacaoConsumer.class);

    private final ValidacaoService validacaoService;
    private final ConversorJson conversorJson;

    public ValidacaoConsumer(ValidacaoService validacaoService, ConversorJson conversorJson) {
        this.validacaoService = validacaoService;
        this.conversorJson = conversorJson;
    }

    @KafkaListener(topics = "${app.kafka.topico.validacao-livro-sucesso}")
    public void validar(String payload) {
        log.info("Recebendo evento para validar livros: {}", payload);
        validacaoService.validar(conversorJson.paraEvento(payload));
    }

    @KafkaListener(topics = "${app.kafka.topico.validacao-livro-falha}")
    public void desfazer(String payload) {
        log.info("Recebendo evento de rollback da validacao de livros: {}", payload);
        validacaoService.desfazer(conversorJson.paraEvento(payload));
    }
}
