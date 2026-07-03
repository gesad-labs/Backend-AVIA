package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.comentario.ComentarioResponseDTO;
import br.com.avia.avia_api.dto.comentario.CriarComentarioRequestDTO;
import br.com.avia.avia_api.entity.Comentario;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoEncontradaException;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoPertenceAoProjetoException;
import br.com.avia.avia_api.repository.ComentarioRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;

    public ComentarioService(
            ComentarioRepository comentarioRepository,
            TarefaRepository tarefaRepository,
            UsuarioRepository usuarioRepository,
            ProjetoUsuarioRepository projetoUsuarioRepository
    ) {
        this.comentarioRepository = comentarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
    }

    public List<ComentarioResponseDTO> listarComentarios(Long projetoId, Long tarefaId) {
        buscarTarefaDoProjeto(projetoId, tarefaId);
        return comentarioRepository.findByTarefaIdOrderByCriadoEmAsc(tarefaId)
                .stream()
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public ComentarioResponseDTO adicionarComentario(Long projetoId, Long tarefaId,
                                                      CriarComentarioRequestDTO dto,
                                                      String emailAutor) {
        buscarTarefaDoProjeto(projetoId, tarefaId);

        Usuario autor = usuarioRepository.findByEmail(emailAutor)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        ProjetoUsuario pu = projetoUsuarioRepository
                .findByUsuarioIdAndProjetoId(autor.getId(), projetoId)
                .orElseThrow(() -> new RuntimeException("Usuário não é membro do projeto."));

        Comentario comentario = new Comentario(tarefaId, dto.descricao(), pu.getId());
        return converterParaDTO(comentarioRepository.save(comentario));
    }

    private ComentarioResponseDTO converterParaDTO(Comentario c) {
        String autorNome  = null;
        String autorEmail = null;
        if (c.getProjetoUsuarioId() != null) {
            ProjetoUsuario pu = projetoUsuarioRepository.findById(c.getProjetoUsuarioId()).orElse(null);
            if (pu != null && pu.getUsuario() != null) {
                autorNome  = pu.getUsuario().getNome();
                autorEmail = pu.getUsuario().getEmail();
            }
        }
        return new ComentarioResponseDTO(
                c.getId(),
                c.getTarefaId(),
                c.getDescricao(),
                autorNome,
                autorEmail,
                c.getCriadoEm()
        );
    }

    private Tarefa buscarTarefaDoProjeto(Long projetoId, Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(TarefaNaoEncontradaException::new);
        if (!Objects.equals(tarefa.getProjetoId(), projetoId))
            throw new TarefaNaoPertenceAoProjetoException();
        return tarefa;
    }
}
