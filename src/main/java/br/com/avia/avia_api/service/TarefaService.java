package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.CriarTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.CriarTarefaBacklogRequestDTO;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.entity.Sprint;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final SprintRepository sprintRepository;

    public TarefaService(TarefaRepository tarefaRepository, SprintRepository sprintRepository) {
        this.tarefaRepository = tarefaRepository;
        this.sprintRepository = sprintRepository;
    }

    @Transactional
    public TarefaResponseDTO criarTarefa(Long projetoId, Long sprintId, CriarTarefaRequestDTO dto) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint não encontrada"));

        if (sprint.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint não pertence a esse projeto");
        }

        Tarefa tarefa = new Tarefa(
                projetoId,
                sprintId,
                dto.titulo(),
                dto.descricao(),
                dto.estimativa(),
                "PENDENTE",
                dto.prioridade().toUpperCase(),
                dto.tarefaParenteId(),
                false
        );

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);

        return converterParaDTO(tarefaSalva);
    }

    @Transactional
    public void removerTarefaDaSprint(Long projetoId, Long sprintId, Long tarefaId) {

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (tarefa.getProjetoId() != projetoId) {
            throw new RuntimeException("Tarefa não pertence a esse projeto");
        }

        if (tarefa.getSprintId() != sprintId) {
            throw new RuntimeException("Tarefa não pertence a essa sprint");
        }

        tarefaRepository.delete(tarefa);
    }

    @Transactional
    public TarefaResponseDTO criarTarefaBacklog(Long projetoId, CriarTarefaBacklogRequestDTO dto) {

        Tarefa tarefa = new Tarefa(
                projetoId,
                0,
                dto.titulo(),
                dto.descricao(),
                0,
                "BACKLOG",
                dto.prioridade().toUpperCase(),
                null,
                false
        );

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);

        return converterParaDTO(tarefaSalva);
    }

    @Transactional
    public List<TarefaResponseDTO> listarBacklog(
            Long projetoId,
            String status,
            String prioridade
    ) {

        List<Tarefa> tarefas = tarefaRepository.findByProjetoIdAndSprintId(projetoId, 0);

        return tarefas.stream()
                .filter(t -> status == null || t.getStatus().equalsIgnoreCase(status))
                .filter(t -> prioridade == null || t.getPrioridade().equalsIgnoreCase(prioridade))
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public TarefaResponseDTO buscarTarefaPorId(Long projetoId, Long tarefaId) {

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (tarefa.getProjetoId() != projetoId) {
            throw new RuntimeException("Tarefa não pertence a esse projeto");
        }

        return converterParaDTO(tarefa);
    }

    private TarefaResponseDTO converterParaDTO(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getSprintId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getEstimativa(),
                tarefa.getStatus(),
                tarefa.getPrioridade(),
                tarefa.getTarefaParenteId(),
                tarefa.isImpedimento()
        );
    }
}