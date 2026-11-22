package com.livraria.usuario.usuario;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.livraria.usuario.usuario.exceptions.MuitasTentativasException;

@Component
public class ControleDeTentativas {

    private static final int LIMITE_DE_FALHAS = 5;
    private static final int MAXIMO_DE_DOBRAS = 20;
    private static final Duration ESPERA_MAXIMA = Duration.ofDays(1);

    private final Map<String, Registro> registros = new ConcurrentHashMap<>();
    private final Duration esperaInicial;
    private final Clock clock;

    public ControleDeTentativas(
            @Value("${app.login.bloqueio-segundos}") long bloqueioSegundos,
            Clock clock) {
        this.esperaInicial = Duration.ofSeconds(bloqueioSegundos);
        this.clock = clock;
    }

    public void exigirLiberado(String email) {
        Registro registro = registros.get(email);

        if (registro != null && registro.bloqueadoEm(agora())) {
            throw new MuitasTentativasException();
        }
    }

    public void registrarFalha(String email) {
        Registro atual = registros.getOrDefault(email, Registro.inicial());

        registros.put(email, atual.comMaisUmaFalha(agora(), esperaInicial));
    }

    public void registrarAcerto(String email) {
        registros.remove(email);
    }

    private Instant agora() {
        return Instant.now(clock);
    }

    private record Registro(int falhas, int bloqueios, Instant liberaEm) {

        static Registro inicial() {
            return new Registro(0, 0, null);
        }

        boolean bloqueadoEm(Instant agora) {
            return liberaEm != null && agora.isBefore(liberaEm);
        }

        Registro comMaisUmaFalha(Instant agora, Duration esperaInicial) {
            int total = falhas + 1;

            if (total < LIMITE_DE_FALHAS) {
                return new Registro(total, bloqueios, null);
            }

            int novosBloqueios = bloqueios + 1;

            return new Registro(0, novosBloqueios, agora.plus(espera(novosBloqueios, esperaInicial)));
        }

        private static Duration espera(int bloqueio, Duration esperaInicial) {
            Duration dobrada = esperaInicial.multipliedBy(1L << Math.min(bloqueio - 1, MAXIMO_DE_DOBRAS));

            return dobrada.compareTo(ESPERA_MAXIMA) > 0 ? ESPERA_MAXIMA : dobrada;
        }
    }
}