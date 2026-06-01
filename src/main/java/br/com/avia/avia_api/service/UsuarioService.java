package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.usuario.AtualizarUsuarioRequestDTO;
import br.com.avia.avia_api.dto.usuario.UsuarioResponse;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UsuarioResponse buscarUsuarioAutenticado(String email) {
        Usuario usuario = buscarPorEmail(email);

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail()
        );
    }

    public UsuarioResponse atualizarUsuarioAutenticado(
            String email,
            AtualizarUsuarioRequestDTO request
    ) {
        Usuario usuario = buscarPorEmail(email);

        if (request.nome() != null && !request.nome().isBlank()) {
            usuario.setNome(request.nome());
        }

        if (request.novaSenha() != null && !request.novaSenha().isBlank()) {
            if (request.senhaAtual() == null || request.senhaAtual().isBlank()) {
                throw new RuntimeException("Informe a senha atual");
            }

            boolean senhaCorreta = passwordEncoder.matches(
                    request.senhaAtual(),
                    usuario.getSenha()
            );

            if (!senhaCorreta) {
                throw new RuntimeException("Senha atual incorreta");
            }

            usuario.setSenha(passwordEncoder.encode(request.novaSenha()));
        }

        Usuario usuarioAtualizado = usuarioRepository.save(usuario);

        return new UsuarioResponse(
                usuarioAtualizado.getId(),
                usuarioAtualizado.getNome(),
                usuarioAtualizado.getEmail()
        );
    }

    private Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}