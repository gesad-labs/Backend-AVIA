package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.auth.*;
import br.com.avia.avia_api.dto.usuario.UsuarioResponse;
import br.com.avia.avia_api.entity.TokenRecuperacaoSenha;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.repository.TokenRecuperacaoSenhaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import br.com.avia.avia_api.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            TokenRecuperacaoSenhaRepository tokenRecuperacaoSenhaRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.tokenRecuperacaoSenhaRepository = tokenRecuperacaoSenhaRepository;
    }

    public UsuarioResponse cadastrar(CadastroRequestDTO request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RuntimeException("E-mail já cadastrado");
        }

        String senhaCriptografada = passwordEncoder.encode(request.senha());

        Usuario usuario = new Usuario(
                request.nome(),
                request.email(),
                senhaCriptografada
        );

        Usuario usuarioSalvo = usuarioRepository.save(usuario);

        return new UsuarioResponse(
                usuarioSalvo.getId(),
                usuarioSalvo.getNome(),
                usuarioSalvo.getEmail()
        );
    }

    public LoginResponseDTO login(LoginRequestDTO request) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(
                request.email(),
                request.senha()
        );

        authenticationManager.authenticate(authenticationToken);

        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String token = tokenService.gerarToken(usuario);

        return new LoginResponseDTO(token);
    }

    public void solicitarRecuperacaoSenha(RecuperacaoSenhaRequestDTO request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElse(null);

        if (usuario == null) {
            return;
        }

        String token = UUID.randomUUID().toString();

        TokenRecuperacaoSenha tokenRecuperacao = new TokenRecuperacaoSenha(
                token,
                LocalDateTime.now().plusMinutes(30),
                usuario
        );

        tokenRecuperacaoSenhaRepository.save(tokenRecuperacao);

        String link = "http://localhost:5173/redefinir-senha?token=" + token;

        System.out.println("Link de recuperação de senha: " + link);

        /*
         * Depois trocar esse System.out.println
         * por um serviço real de envio de e-mail.
         */
    }

    public void redefinirSenha(RedefinicaoSenhaRequestDTO request) {
        TokenRecuperacaoSenha tokenRecuperacao = tokenRecuperacaoSenhaRepository
                .findByToken(request.token())
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (tokenRecuperacao.isUsado()) {
            throw new RuntimeException("Token já utilizado");
        }

        if (tokenRecuperacao.expirado()) {
            throw new RuntimeException("Token expirado");
        }

        Usuario usuario = tokenRecuperacao.getUsuario();

        usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        usuarioRepository.save(usuario);

        tokenRecuperacao.setUsado(true);
        tokenRecuperacaoSenhaRepository.save(tokenRecuperacao);
    }
}