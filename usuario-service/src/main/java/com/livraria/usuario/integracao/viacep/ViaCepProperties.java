package com.livraria.usuario.integracao.viacep;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.viacep")
@Getter
@Setter
public class ViaCepProperties {

    private String url = "https://viacep.com.br/ws";

    private int connectTimeoutMs = 3000;

    private int readTimeoutMs = 5000;
}
