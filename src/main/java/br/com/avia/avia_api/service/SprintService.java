package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.sprint.*;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.entity.Projeto;
import br.com.avia.avia_api.entity.Sprint;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private static final String STATUS_BACKLOG = "Backlog";

    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TarefaRepository tarefaRepository;
    private final TarefaService tarefaService;

    public SprintService(SprintRepository sprintRepository,
                         ProjetoRepository projetoRepository,
                         UsuarioRepository usuarioRepository,
                         TarefaRepository tarefaRepository,
                         TarefaService tarefaService) {
        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.tarefaService = tarefaService;
    }

    @Transactional
    public SprintResponseDTO criarSprint(Long projetoId, CriarSprintRequestDTO dto, String emailUsuario) {

        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        boolean sprintJaExiste = sprintRepository
                .existsByProjetoIdAndNome(projetoId, dto.nome());

        if (sprintJaExiste) {
            throw new RuntimeException("Já existe uma sprint com esse nome neste projeto");
        }

        Sprint sprint = new Sprint(
                projeto.getId(),
                dto.nome(),
                "Planejada",
                dto.dataInicio(),
                dto.dataFim()
        );

        Sprint sprintSalva = sprintRepository.save(sprint);

        return converterParaDTO(sprintSalva);
    }


    @Transactional
    public List<SprintResponseDTO> listarSprintsPorProjeto(Long projetoId, String emailUsuario) {

        Usuario usuarioLogado = buscarUsuarioPorEmail(emailUsuario);

        Projeto projeto = projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        List<Sprint> sprints = sprintRepository.findByProjetoId(projetoId);

        return sprints.stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SprintResponseDTO editarSprint(Long projetoId, Long sprintId, EditarSprintRequestDTO dto, String emailUsuario) {
        buscarUsuarioPorEmail(emailUsuario);

        projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint não encontrada"));

        if (sprint.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint não pertence a esse projeto");
        }
        if (dto.nome() != null) sprint.setNome(dto.nome());
        if (dto.status() != null) sprint.setStatus(dto.status());
        if (dto.dataInicio() != null) sprint.setDataInicio(dto.dataInicio());
        if (dto.dataFim() != null) sprint.setDataFim(dto.dataFim());

        Sprint sprintAtualizada = sprintRepository.save(sprint);

        return converterParaDTO(sprintAtualizada);
    }

    @Transactional
    public BacklogResponseDTO getOuCriarBacklog(Long projetoId, String emailUsuario) {
        buscarUsuarioPorEmail(emailUsuario);

        projetoRepository.findById(projetoId)
                .orElseThrow(() -> new RuntimeException("Projeto não encontrado"));

        Sprint backlogSprint = sprintRepository
                .findByProjetoIdAndStatus(projetoId, STATUS_BACKLOG)
                .orElseGet(() -> {
                    Sprint nova = new Sprint(projetoId, "Backlog", STATUS_BACKLOG);
                    return sprintRepository.save(nova);
                });

        List<TarefaResponseDTO> tarefas = tarefaRepository
                .findBySprintId(backlogSprint.getId())
                .stream()
                .map(tarefaService::converterParaDTO)
                .collect(Collectors.toList());

        return new BacklogResponseDTO(backlogSprint.getId(), tarefas);
    }

    private SprintResponseDTO converterParaDTO(Sprint sprint) {
        return new SprintResponseDTO(
                sprint.getId(),
                sprint.getNome(),
                sprint.getProjetoId(),
                sprint.getStatus(),
                sprint.getDataInicio(),
                sprint.getDataFim()
        );
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }
}