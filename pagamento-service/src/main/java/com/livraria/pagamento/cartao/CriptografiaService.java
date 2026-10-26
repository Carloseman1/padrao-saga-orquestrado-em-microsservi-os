package com.livraria.pagamento.cartao;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;

@Service
public class CriptografiaService {

    private final TextEncryptor encryptor;

    public CriptografiaService(
            @Value("${app.cripto.chave}") String chave,
            @Value("${app.cripto.salt}") String salt) {
        this.encryptor = Encryptors.delux(chave, salt);
    }

    public String criptografar(String texto) {
        return encryptor.encrypt(texto);
    }

    public String descriptografar(String texto) {
        return encryptor.decrypt(texto);
    }
}
