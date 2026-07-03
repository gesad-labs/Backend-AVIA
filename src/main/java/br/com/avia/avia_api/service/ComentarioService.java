package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.comentario.CriarComentarioRequestDTO;
import br.com.avia.avia_api.dto.comentario.ComentarioResponseDTO;
import br.com.avia.avia_api.entity.Comentario;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.repository.ComentarioRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             TarefaRepository tarefaRepository,
                             UsuarioRepository usuarioRepository) {
        this.comentarioRepository = comentarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ComentarioResponseDTO criarComentario(Long tarefaId, CriarComentarioRequestDTO dto, String emailUsuario) {
        tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Comentario comentario = new Comentario(tarefaId, dto.descricao(), usuario.getId().intValue());
        Comentario salvo = comentarioRepository.save(comentario);
        return converterParaDTO(salvo);
    }

    public List<ComentarioResponseDTO> listarComentarios(Long tarefaId) {
        tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        return comentarioRepository.findByTarefaIdOrderByIdAsc(tarefaId)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ComentarioResponseDTO alterarFlag(Long comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
        comentario.setImpedimento(!comentario.isImpedimento());
        return converterParaDTO(comentarioRepository.save(comentario));
    }

    @Transactional
    public void deletarComentario(Long tarefaId, Long comentarioId) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
        if (!comentario.getTarefaId().equals(tarefaId)) {
            throw new RuntimeException("Comentário não pertence a essa tarefa");
        }
        comentarioRepository.delete(comentario);
    }

    @Transactional
    public ComentarioResponseDTO editarComentario(Long tarefaId, Long comentarioId, String novaDescricao) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new RuntimeException("Comentário não encontrado"));
        if (!comentario.getTarefaId().equals(tarefaId)) {
            throw new RuntimeException("Comentário não pertence a essa tarefa");
        }
        comentario.setDescricao(novaDescricao);
        return converterParaDTO(comentarioRepository.save(comentario));
    }

    private ComentarioResponseDTO converterParaDTO(Comentario comentario) {
        return new ComentarioResponseDTO(
                comentario.getId(),
                comentario.getTarefaId(),
                comentario.getDescricao(),
                comentario.getProjetoUsuarioId(),
                comentario.isImpedimento()
        );
    }
}