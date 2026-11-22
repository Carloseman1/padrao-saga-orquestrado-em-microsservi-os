package com.livraria.usuario.integracao.viacep;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class ViaCepConfig {

    @Bean
    RestClient viacepRestClient(ViaCepProperties properties){
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(properties.getConnectTimeoutMs());
        fabrica.setReadTimeout(properties.getReadTimeoutMs());

        return  RestClient.builder()
                .baseUrl(properties.getUrl())
                .requestFactory(fabrica)
                .build();
    }
}
