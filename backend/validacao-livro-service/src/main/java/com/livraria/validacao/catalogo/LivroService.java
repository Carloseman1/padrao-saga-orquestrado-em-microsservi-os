package com.livraria.validacao.catalogo;

import java.math.BigDecimal;
import java.util.List;

import com.livraria.validacao.catalogo.exceptions.PrecoInvalidoException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livraria.validacao.catalogo.dto.LivroDTO;
import com.livraria.validacao.catalogo.dto.LivroRequest;
import com.livraria.validacao.catalogo.exceptions.LivroJaCadastradoException;
import com.livraria.validacao.catalogo.exceptions.QuantidadeInicialInvalidaException;
import com.livraria.validacao.shared.exception.CampoObrigatorioException;

@Service
public class LivroService {

    private final LivroRepository livroRepository;
    private final LivroCadastradoProducer livroCadastradoProducer;

    public LivroService(LivroRepository livroRepository, LivroCadastradoProducer livroCadastradoProducer) {
        this.livroRepository = livroRepository;
        this.livroCadastradoProducer = livroCadastradoProducer;
    }

    @Transactional
    public LivroDTO cadastrar(LivroRequest request) {
        String codigo = obrigatorio(request.codigo(), "código do livro").toUpperCase();
        String titulo = obrigatorio(request.titulo(), "título do livro");

        if (request.quantidadeInicial() < 0) {
            throw new QuantidadeInicialInvalidaException();
        }

        if (request.preco() == null || request.preco().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PrecoInvalidoException();
        }

        if (livroRepository.existsByCodigo(codigo)) {
            throw new LivroJaCadastradoException();
        }

        Livro livro = livroRepository.save(new Livro(codigo, titulo, request.preco()));
        livroCadastradoProducer.enviar(new LivroCadastrado(codigo, titulo, request.quantidadeInicial()));

        return LivroDTO.de(livro);
    }

    public List<LivroDTO> listar() {
        return livroRepository.findAll(Sort.by("titulo")).stream()
                .map(LivroDTO::de)
                .toList();
    }

    private String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException(campo);
        }
        return valor.trim();
    }
}
