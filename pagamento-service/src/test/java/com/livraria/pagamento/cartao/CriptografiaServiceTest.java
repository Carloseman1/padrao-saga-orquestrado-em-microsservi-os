package com.livraria.pagamento.cartao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CriptografiaServiceTest {

    private static final String NUMERO = "4111111111111111";

    private final CriptografiaService criptografia =
            new CriptografiaService("chave-de-teste", "5c0744940b5c369b");

    @Test
    void criptografa_e_descriptografa_de_volta() {
        String cifrado = criptografia.criptografar(NUMERO);

        assertThat(criptografia.descriptografar(cifrado)).isEqualTo(NUMERO);
    }

    @Test
    void o_texto_cifrado_nao_contem_o_original() {
        String cifrado = criptografia.criptografar(NUMERO);

        assertThat(cifrado).isNotEqualTo(NUMERO).doesNotContain(NUMERO);
    }

    @Test
    void duas_cifragens_do_mesmo_numero_saem_diferentes() {
        String primeira = criptografia.criptografar(NUMERO);
        String segunda = criptografia.criptografar(NUMERO);

        assertThat(primeira).isNotEqualTo(segunda);
        assertThat(criptografia.descriptografar(primeira))
                .isEqualTo(criptografia.descriptografar(segunda));
    }
}
