package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.projeto.AtualizarProjetoRequestDTO;
import br.com.avia.avia_api.dto.projeto.CriarProjetoRequestDTO;
import br.com.avia.avia_api.dto.projeto.ProjetoResponseDTO;
import br.com.avia.avia_api.entity.Projeto;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;

    public ProjetoService(ProjetoRepository projetoRepository, UsuarioRepository usuarioRepository) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ProjetoResponseDTO criarProjeto(CriarProjetoRequestDTO dto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        Projeto projeto = new Projeto();
        projeto.setNome(dto.nome());
        projeto.setTipoEstimativa(dto.tipoEstimativa());
        projeto.setGerente(usuarioLogado);

        ProjetoUsuario vinculoGerente = new ProjetoUsuario("gerente", usuarioLogado, projeto);
        projeto.getMembros().add(vinculoGerente);

        Projeto projetoSalvo = projetoRepository.save(projeto);

        return converterParaDTO(projetoSalvo);
    }

    public ProjetoResponseDTO buscarPorId(Long projetoId, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        boolean isMembro = projeto.getMembros().stream()
                .anyMatch(pu -> pu.getUsuario().getId() == usuarioLogado.getId());

        if (!isMembro) {
            throw new RuntimeException("Você não tem permissão para visualizar este projeto.");
        }

        return converterParaDTO(projeto);
    }

    public List<ProjetoResponseDTO> listarProjetosDoUsuario(String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);
        List<Projeto> projetos = projetoRepository.findProjetosByMembroId(usuarioLogado.getId());

        return projetos.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletarProjeto(Long projetoId, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        if (projeto.getGerente().getId() != usuarioLogado.getId()) {
            throw new RuntimeException("Apenas o criador pode deletar este projeto.");
        }

        projetoRepository.delete(projeto);
    }

    @Transactional
    public ProjetoResponseDTO editarInformacoesDoProjeto(Long projetoId, AtualizarProjetoRequestDTO dto, String emailUsuario) {
        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);
        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado."));

        if (projeto.getGerente().getId() != usuarioLogado.getId()) {
            throw new RuntimeException("Apenas o criador pode atualizar os dados deste projeto.");
        }

        if (dto.nome() != null && !dto.nome().isBlank()) {
            projeto.setNome(dto.nome());
        }

        if (dto.tipoEstimativa() != null && !dto.tipoEstimativa().isBlank()) {
            projeto.setTipoEstimativa(dto.tipoEstimativa());
        }

        Projeto projetoAtualizado = projetoRepository.save(projeto);
        return converterParaDTO(projetoAtualizado);
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado na base de dados."));
    }

    private ProjetoResponseDTO converterParaDTO(Projeto projeto) {
        return new ProjetoResponseDTO(
                projeto.getId(),
                projeto.getNome(),
                projeto.getTipoEstimativa()
        );
    }
}