package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.AtualizarTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.CriarTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.entity.Sprint;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
        Tarefa tarefa = new Tarefa(sprint.getId(), dto.titulo(), dto.estimativa(), "pendente",
                dto.tarefaParenteId(), false);
        tarefa.setPrioridade(dto.prioridade());
        tarefa.setTipo(dto.tipo());
        Tarefa tarefaSalva = tarefaRepository.save(tarefa);
        return converterParaDTO(tarefaSalva);
    }

    public List<TarefaResponseDTO> listarTarefas(Long projetoId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint não encontrada"));
        if (sprint.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint não pertence a esse projeto");
        }
        return tarefaRepository.findBySprintId(sprintId)
                .stream()
                .map(this::converterParaDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TarefaResponseDTO atualizarTarefa(Long projetoId, Long sprintId, Long tarefaId, AtualizarTarefaRequestDTO dto) {
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
        if (dto.titulo() != null) tarefa.setTitulo(dto.titulo());
        if (dto.estimativa() != null) tarefa.setEstimativa(dto.estimativa());
        if (dto.status() != null) tarefa.setStatus(dto.status());
        if (dto.prioridade() != null) tarefa.setPrioridade(dto.prioridade());
        if (dto.tipo() != null) tarefa.setTipo(dto.tipo());
        tarefa.setImpedimento(dto.impedimento());
        return converterParaDTO(tarefaRepository.save(tarefa));
    }

    @Transactional
    public TarefaResponseDTO moverTarefaParaSprint(Long projetoId, Long sprintId, Long tarefaId, Long novaSprintId) {
        Sprint sprintOrigem = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint de origem não encontrada"));
        if (sprintOrigem.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint de origem não pertence a esse projeto");
        }

        Sprint sprintDestino = sprintRepository.findById(novaSprintId)
                .orElseThrow(() -> new RuntimeException("Sprint de destino não encontrada"));
        if (sprintDestino.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint de destino não pertence a esse projeto");
        }

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));
        if (tarefa.getSprintId() != sprintId) {
            throw new RuntimeException("Tarefa não pertence a sprint de origem");
        }

        tarefa.setSprintId(novaSprintId);
        Tarefa salva = tarefaRepository.save(tarefa);
        return converterParaDTO(salva);
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

    public TarefaResponseDTO converterParaDTO(Tarefa tarefa) {
        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getSprintId(),
                tarefa.getTitulo(),
                tarefa.getEstimativa(),
                tarefa.getStatus(),
                tarefa.getTarefaParenteId(),
                tarefa.isImpedimento(),
                tarefa.isEstimativaConsolidada(),
                tarefa.getPrioridade(),
                tarefa.getTipo()
        );
    }
}
