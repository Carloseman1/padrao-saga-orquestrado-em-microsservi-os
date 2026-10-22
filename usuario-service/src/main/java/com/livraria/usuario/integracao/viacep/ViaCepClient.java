package com.livraria.usuario.integracao.viacep;

import java.net.SocketTimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.livraria.usuario.integracao.viacep.exceptions.ViaCepIndisponivelException;
import com.livraria.usuario.integracao.viacep.exceptions.ViaCepRespostaInesperadaException;
import com.livraria.usuario.integracao.viacep.exceptions.ViaCepTimeoutException;

@Component
public class ViaCepClient {

    private static final Logger log = LoggerFactory.getLogger(ViaCepClient.class);

    private final RestClient restClient;

    public ViaCepClient(RestClient viaCepRestClient) {
        this.restClient = viaCepRestClient;
    }

    public ViaCepResponse consultar(String cep) {
        try {
            return restClient.get()
                    .uri("/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResponse.class);

        } catch (ResourceAccessException ex) {
            if (ex.getCause() instanceof SocketTimeoutException) {
                log.warn("ViaCEP nao respondeu no tempo limite para o CEP {}", cep);
                throw new ViaCepTimeoutException(ex);
            }
            log.warn("Falha de comunicacao com o ViaCEP para o CEP {}", cep, ex);
            throw new ViaCepIndisponivelException(ex);

        } catch (RestClientResponseException ex) {
            log.warn("ViaCEP respondeu {} para o CEP {}", ex.getStatusCode(), cep);
            if (ex.getStatusCode().is5xxServerError()) {
                throw new ViaCepIndisponivelException(ex);
            }
            throw new ViaCepRespostaInesperadaException(ex);
        }
    }
}