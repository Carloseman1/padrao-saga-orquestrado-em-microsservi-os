package com.livraria.usuario.usuario;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livraria.usuario.endereco.Endereco;
import com.livraria.usuario.endereco.EnderecoService;
import com.livraria.usuario.seguranca.JwtService;
import com.livraria.usuario.shared.exception.CampoObrigatorioException;
import com.livraria.usuario.usuario.dto.LoginRequest;
import com.livraria.usuario.usuario.dto.LoginResponseDTO;
import com.livraria.usuario.usuario.dto.RecuperacaoRequest;
import com.livraria.usuario.usuario.dto.RedefinicaoRequest;
import com.livraria.usuario.usuario.dto.UsuarioDTO;
import com.livraria.usuario.usuario.dto.UsuarioRequest;
import com.livraria.usuario.usuario.exceptions.CredenciaisInvalidasException;
import com.livraria.usuario.usuario.exceptions.EmailCadastradoException;
import com.livraria.usuario.usuario.exceptions.SenhaForaDoPadraoException;
import com.livraria.usuario.usuario.exceptions.SenhaVaziaException;
import com.livraria.usuario.usuario.exceptions.TokenRecuperacaoInvalidoException;

@Service
public class UsuarioService {

    private static final String PADRAO_SENHA =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!*]).{8,72}$";

    private static final Logger log = LoggerFactory.getLogger(UsuarioService.class);

    private final UsuarioRepository usuarioRepository;
    private final RecuperacaoSenhaRepository recuperacaoRepository;
    private final EnderecoService enderecoService;
    private final JwtService jwtService;
    private final ControleDeTentativas controleDeTentativas;
    private final Clock clock;
    private final long expiracaoDaRecuperacaoEmMinutos;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            RecuperacaoSenhaRepository recuperacaoRepository,
            EnderecoService enderecoService,
            JwtService jwtService,
            ControleDeTentativas controleDeTentativas,
            Clock clock,
            @Value("${app.recuperacao.expiracao-minutos}") long expiracaoDaRecuperacaoEmMinutos) {
        this.usuarioRepository = usuarioRepository;
        this.recuperacaoRepository = recuperacaoRepository;
        this.enderecoService = enderecoService;
        this.jwtService = jwtService;
        this.controleDeTentativas = controleDeTentativas;
        this.clock = clock;
        this.expiracaoDaRecuperacaoEmMinutos = expiracaoDaRecuperacaoEmMinutos;
    }

    public UsuarioDTO cadastrar(UsuarioRequest request) {
        Email email = new Email(request.email());
        validarSenha(request.senha());

        String nome = obrigatorio(request.nome(), "nome");
        String sobrenome = obrigatorio(request.sobrenome(), "sobrenome");
        String numero = obrigatorio(request.numero(), "número do endereço");

        if (usuarioRepository.findByEmail(email.valor()).isPresent()) {
            throw new EmailCadastradoException();
        }

        Endereco endereco = enderecoService.buscar(request.cep())
                .completadoCom(numero, complementoLimpo(request.complemento()));

        Usuario usuario = new Usuario(nome, sobrenome, email.valor(),
                encoder.encode(request.senha()), endereco);

        try {
            return UsuarioDTO.de(usuarioRepository.save(usuario));
        } catch (DataIntegrityViolationException ex) {
            throw new EmailCadastradoException();
        }
    }

    public LoginResponseDTO login(LoginRequest request) {
        if (request.email() == null || request.senha() == null) {
            throw new CredenciaisInvalidasException();
        }

        String email = emailNormalizado(request.email());
        controleDeTentativas.exigirLiberado(email);

        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        if (usuario == null || !usuario.isAtivo()
                || !encoder.matches(request.senha(), usuario.getSenha())) {
            controleDeTentativas.registrarFalha(email);
            throw new CredenciaisInvalidasException();
        }

        controleDeTentativas.registrarAcerto(email);

        return new LoginResponseDTO(
                usuario.getId(),
                usuario.getNomeCompleto(),
                usuario.getEmail(),
                jwtService.gerarToken(usuario.getEmail(), usuario.getId()));
    }

    @Transactional
    public void solicitarRecuperacao(RecuperacaoRequest request) {
        Usuario usuario = usuarioPeloEmail(request.email());

        if (usuario == null || !usuario.isAtivo()) {
            return;
        }

        recuperacaoRepository.deleteByUsuarioIdAndUsadoEmIsNull(usuario.getId());

        String token = UUID.randomUUID().toString();
        LocalDateTime expiraEm =
                LocalDateTime.now(clock).plusMinutes(expiracaoDaRecuperacaoEmMinutos);

        recuperacaoRepository.save(new RecuperacaoSenha(usuario, token, expiraEm));

        log.info("Token de recuperacao de senha para {}: {} (expira em {})",
                usuario.getEmail(), token, expiraEm);
    }

    @Transactional
    public void redefinirSenha(RedefinicaoRequest request) {
        RecuperacaoSenha recuperacao = recuperacaoRepository
                .findByToken(request.token() == null ? "" : request.token().trim())
                .filter(guardada -> guardada.estaValida(LocalDateTime.now(clock)))
                .orElseThrow(TokenRecuperacaoInvalidoException::new);

        validarSenha(request.senha());

        Usuario usuario = recuperacao.getUsuario();
        usuario.setSenha(encoder.encode(request.senha()));
        usuarioRepository.save(usuario);

        recuperacao.marcarUsada(LocalDateTime.now(clock));
        recuperacaoRepository.save(recuperacao);

        controleDeTentativas.registrarAcerto(usuario.getEmail());
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new SenhaVaziaException();
        }

        if (!senha.matches(PADRAO_SENHA)) {
            throw new SenhaForaDoPadraoException();
        }
    }

    private String obrigatorio(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new CampoObrigatorioException(campo);
        }
        return valor.trim();
    }

    private String complementoLimpo(String complemento) {
        if (complemento == null || complemento.isBlank()) {
            return null;
        }
        return complemento.trim();
    }

    private String emailNormalizado(String email) {
        try {
            return new Email(email).valor();
        } catch (RuntimeException ex) {
            throw new CredenciaisInvalidasException();
        }
    }

    private Usuario usuarioPeloEmail(String email) {
        try {
            return usuarioRepository.findByEmail(new Email(email).valor()).orElse(null);
        } catch (RuntimeException ex) {
            return null;
        }
    }
}