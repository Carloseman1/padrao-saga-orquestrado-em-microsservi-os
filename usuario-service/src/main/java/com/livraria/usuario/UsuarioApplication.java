package com.livraria.usuario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UsuarioApplication {

    private static final String TIPO_DE_TRUSTSTORE = "javax.net.ssl.trustStoreType";

    public static void main(String[] args) {
        confiarNosCertificadosDoWindows();
        SpringApplication.run(UsuarioApplication.class, args);
    }

    private static void confiarNosCertificadosDoWindows() {
        boolean noWindows = System.getProperty("os.name", "").startsWith("Windows");

        if (noWindows && System.getProperty(TIPO_DE_TRUSTSTORE) == null) {
            System.setProperty(TIPO_DE_TRUSTSTORE, "Windows-ROOT");
        }
    }
}
