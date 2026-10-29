package com.livraria.fiscal.sefaz;

import java.math.BigDecimal;
import java.util.List;

public record SolicitacaoAutorizacao(String chaveAcesso, BigDecimal valorTotal, List<String> codigosDosLivros) {
}
