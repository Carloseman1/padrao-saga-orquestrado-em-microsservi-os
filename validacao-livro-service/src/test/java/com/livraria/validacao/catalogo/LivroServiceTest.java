package com.livraria.validacao.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.validacao.catalogo.dto.LivroDTO;
import com.livraria.validacao.catalogo.dto.LivroRequest;
import com.livraria.validacao.catalogo.exceptions.LivroJaCadastradoException;
import com.livraria.validacao.catalogo.exceptions.QuantidadeInicialInvalidaException;
import com.livraria.validacao.shared.exception.CampoObrigatorioException;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    @Mock
    private LivroRepository livroRepository;

    @Mock
    private LivroCadastradoProducer livroCadastradoProducer;

    private LivroService livroService;

    @BeforeEach
    void criarService() {
        livroService = new LivroService(livroRepository, livroCadastradoProducer);
    }

    @Test
    void cadastra_livro_com_codigo_em_maiusculas() {
        devolverOLivroSalvo();

        LivroDTO cadastrado = livroService.cadastrar(new LivroRequest(" o_alienista ", "O Alienista", 10));

        assertThat(cadastrado.codigo()).isEqualTo("O_ALIENISTA");
        assertThat(cadastrado.titulo()).isEqualTo("O Alienista");
    }

    @Test
    void avisa_o_estoque_com_a_quantidade_inicial() {
        devolverOLivroSalvo();

        livroService.cadastrar(new LivroRequest("O_ALIENISTA", "O Alienista", 10));

        verify(livroCadastradoProducer).enviar(new LivroCadastrado("O_ALIENISTA", "O Alienista", 10));
    }

    @Test
    void recusa_codigo_que_ja_existe() {
        when(livroRepository.existsByCodigo("DOM_CASMURRO")).thenReturn(true);

        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest("DOM_CASMURRO", "Dom Casmurro", 1)))
                .isInstanceOf(LivroJaCadastradoException.class);

        verify(livroRepository, never()).save(any());
        verify(livroCadastradoProducer, never()).enviar(any());
    }

    @Test
    void recusa_quantidade_inicial_negativa() {
        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest("O_ALIENISTA", "O Alienista", -1)))
                .isInstanceOf(QuantidadeInicialInvalidaException.class);
    }

    @Test
    void recusa_livro_sem_codigo() {
        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest(" ", "O Alienista", 1)))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void recusa_livro_sem_titulo() {
        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest("O_ALIENISTA", null, 1)))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    private void devolverOLivroSalvo() {
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }
}
