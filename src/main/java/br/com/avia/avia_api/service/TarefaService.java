package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.CriarTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.entity.Sprint;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
        Tarefa tarefa = new Tarefa(sprint.getId(), dto.titulo(), dto.estimativa(), "pendente", null, false);
        Tarefa tarefaSalva = tarefaRepository.save(tarefa);
        return converterParaDTO(tarefaSalva);
    }

    @Transactional
    public void removerTarefaDaSprint(Long projetoId, Long sprintId, Long tarefaId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint não encontrada"));
        if (sprint.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint não pertence a esse projeto");
        }
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        if (tarefa.getSprintId() != sprintId) {
            throw new RuntimeException("Tarefa não pertence a essa sprint");
        }
        tarefaRepository.delete(tarefa);
    }

    private TarefaResponseDTO converterParaDTO(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getSprintId(),
                tarefa.getTitulo(),
                tarefa.getEstimativa(),
                tarefa.getStatus(),
                tarefa.getTarefaParenteId(),
                tarefa.isImpedimento()
        );
    }
}