package com.livraria.validacao.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.livraria.validacao.catalogo.dto.LivroDTO;
import com.livraria.validacao.catalogo.dto.LivroRequest;
import com.livraria.validacao.catalogo.exceptions.LivroJaCadastradoException;
import com.livraria.validacao.catalogo.exceptions.LivroNaoEncontradoException;
import com.livraria.validacao.catalogo.exceptions.PrecoInvalidoException;
import com.livraria.validacao.catalogo.exceptions.QuantidadeInicialInvalidaException;
import com.livraria.validacao.shared.exception.CampoObrigatorioException;

@ExtendWith(MockitoExtension.class)
class LivroServiceTest {

    private static final BigDecimal PRECO = new BigDecimal("32.50");

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
    void busca_o_livro_pelo_codigo_com_os_detalhes_do_catalogo() {
        Livro livro = new Livro("SAPIENS", "Sapiens", PRECO);
        livro.setAutor("Yuval Noah Harari");
        livro.setCategoria(CategoriaLivro.HISTORIA);
        livro.setCapaUrl("https://covers.openlibrary.org/b/id/8634250-L.jpg");
        when(livroRepository.findByCodigo("SAPIENS")).thenReturn(Optional.of(livro));

        LivroDTO encontrado = livroService.buscar("sapiens");

        assertThat(encontrado.autor()).isEqualTo("Yuval Noah Harari");
        assertThat(encontrado.categoria()).isEqualTo(CategoriaLivro.HISTORIA);
        assertThat(encontrado.capaUrl()).endsWith("8634250-L.jpg");
    }

    @Test
    void livro_fora_do_catalogo_responde_nao_encontrado() {
        when(livroRepository.findByCodigo("INEXISTENTE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> livroService.buscar("INEXISTENTE"))
                .isInstanceOf(LivroNaoEncontradoException.class);
    }

    @Test
    void livro_cadastrado_sem_categoria_fica_em_outros() {
        assertThat(new Livro("O_ALIENISTA", "O Alienista", PRECO).getCategoria()).isEqualTo(CategoriaLivro.OUTROS);
    }

    @Test
    void cadastra_livro_com_codigo_em_maiusculas() {
        devolverOLivroSalvo();

        LivroDTO cadastrado = livroService.cadastrar(new LivroRequest(" o_alienista ", "O Alienista", PRECO, 10));

        assertThat(cadastrado.codigo()).isEqualTo("O_ALIENISTA");
        assertThat(cadastrado.titulo()).isEqualTo("O Alienista");
        assertThat(cadastrado.preco()).isEqualByComparingTo("32.50");
    }

    @Test
    void avisa_o_estoque_com_a_quantidade_inicial() {
        devolverOLivroSalvo();

        livroService.cadastrar(new LivroRequest("O_ALIENISTA", "O Alienista", PRECO, 10));

        verify(livroCadastradoProducer).enviar(new LivroCadastrado("O_ALIENISTA", "O Alienista", 10));
    }

    @Test
    void recusa_codigo_que_ja_existe() {
        when(livroRepository.existsByCodigo("DOM_CASMURRO")).thenReturn(true);

        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest("DOM_CASMURRO", "Dom Casmurro", PRECO, 1)))
                .isInstanceOf(LivroJaCadastradoException.class);

        verify(livroRepository, never()).save(any());
        verify(livroCadastradoProducer, never()).enviar(any());
    }

    @Test
    void recusa_quantidade_inicial_negativa() {
        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest("O_ALIENISTA", "O Alienista", PRECO, -1)))
                .isInstanceOf(QuantidadeInicialInvalidaException.class);
    }

    @Test
    void recusa_livro_sem_codigo() {
        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest(" ", "O Alienista", PRECO, 1)))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void recusa_livro_sem_titulo() {
        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest("O_ALIENISTA", null, PRECO, 1)))
                .isInstanceOf(CampoObrigatorioException.class);
    }

    @Test
    void recusa_livro_sem_preco() {
        assertThatThrownBy(() -> livroService.cadastrar(new LivroRequest("O_ALIENISTA", "O Alienista", null, 1)))
                .isInstanceOf(PrecoInvalidoException.class);
    }

    @Test
    void recusa_preco_zero() {
        assertThatThrownBy(() -> livroService.cadastrar(
                new LivroRequest("O_ALIENISTA", "O Alienista", BigDecimal.ZERO, 1)))
                .isInstanceOf(PrecoInvalidoException.class);
    }

    private void devolverOLivroSalvo() {
        when(livroRepository.save(any(Livro.class))).thenAnswer(invocacao -> invocacao.getArgument(0));
    }
}
