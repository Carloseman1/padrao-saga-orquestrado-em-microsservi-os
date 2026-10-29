package com.livraria.fiscal.sefaz;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SefazSimulada implements Sefaz {

    private static final Logger log = LoggerFactory.getLogger(SefazSimulada.class);

    private final List<String> livrosComNcmInvalido;

    public SefazSimulada(@Value("${app.sefaz.livros-com-ncm-invalido}") List<String> livrosComNcmInvalido) {
        this.livrosComNcmInvalido = livrosComNcmInvalido;
    }

    @Override
    public RespostaSefaz autorizar(SolicitacaoAutorizacao solicitacao) {
        boolean ncmInvalido = solicitacao.codigosDosLivros().stream().anyMatch(livrosComNcmInvalido::contains);

        if (ncmInvalido) {
            log.info("SEFAZ simulada rejeitou a NF-e {}: NCM inexistente", solicitacao.chaveAcesso());
            return RespostaSefaz.recusada("778", "Informado NCM inexistente");
        }

        log.info("SEFAZ simulada autorizou a NF-e {}", solicitacao.chaveAcesso());
        return RespostaSefaz.aceita("100", "Autorizado o uso da NF-e", novoProtocolo());
    }

    @Override
    public RespostaSefaz cancelar(String chaveAcesso, String justificativa) {
        log.info("SEFAZ simulada registrou o cancelamento da NF-e {}: {}", chaveAcesso, justificativa);
        return RespostaSefaz.aceita("135", "Evento registrado e vinculado a NF-e", novoProtocolo());
    }

    private String novoProtocolo() {
        return "135" + String.format("%012d", ThreadLocalRandom.current().nextLong(1_000_000_000_000L));
    }
}
