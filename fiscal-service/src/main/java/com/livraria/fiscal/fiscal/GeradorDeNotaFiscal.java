package com.livraria.fiscal.fiscal;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.livraria.fiscal.sefaz.ChaveDeAcesso;

@Component
public class GeradorDeNotaFiscal {

    private final NotaFiscalRepository notaFiscalRepository;
    private final int uf;
    private final String cnpj;
    private final int serie;
    private final Clock clock;

    public GeradorDeNotaFiscal(NotaFiscalRepository notaFiscalRepository,
                               @Value("${app.emitente.uf}") int uf,
                               @Value("${app.emitente.cnpj}") String cnpj,
                               @Value("${app.emitente.serie}") int serie,
                               Clock clock) {
        this.notaFiscalRepository = notaFiscalRepository;
        this.uf = uf;
        this.cnpj = cnpj;
        this.serie = serie;
        this.clock = clock;
    }

    public NotaFiscal novaNota(String pedidoId, String transacaoId, BigDecimal valorTotal) {
        long numero = notaFiscalRepository.proximoNumero();
        int codigoNumerico = ThreadLocalRandom.current().nextInt(100_000_000);
        ChaveDeAcesso chave = ChaveDeAcesso.gerar(uf, YearMonth.now(clock), cnpj, serie, numero, codigoNumerico);

        return new NotaFiscal(pedidoId, transacaoId, numero, serie, chave.valor(), valorTotal,
                LocalDateTime.now(clock));
    }
}
