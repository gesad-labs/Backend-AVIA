package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.*;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.exception.sprint.SprintNaoEncontradaException;
import br.com.avia.avia_api.exception.sprint.SprintNaoPertenceAoProjetoException;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoEncontradaException;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoPertenceAoProjetoException;
import br.com.avia.avia_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final SprintRepository sprintRepository;

    public TarefaService(
            TarefaRepository tarefaRepository,
            SprintRepository sprintRepository
    ) {
        this.tarefaRepository = tarefaRepository;
        this.sprintRepository = sprintRepository;
    }

    @Transactional
    public TarefaResponseDTO criarTarefa(Long projetoId, CriarTarefaRequestDTO dto) {

        Long sprintId = dto.sprintId();
        Long tarefaParenteId = dto.tarefaParenteId();

        if (sprintId != null) {
            Sprint sprint = sprintRepository.findById(sprintId)
                    .orElseThrow(SprintNaoEncontradaException::new);

            if (!Objects.equals(sprint.getProjetoId(), projetoId)) {
                throw new SprintNaoPertenceAoProjetoException();
            }
        }

        if (tarefaParenteId != null) {
            buscarTarefaDoProjeto(projetoId, tarefaParenteId);
        }

        Tarefa tarefa = new Tarefa(
                projetoId,
                dto.sprintId(),
                dto.titulo(),
                dto.descricao(),
                dto.estimativa(),
                dto.status(),
                dto.prioridade(),
                tarefaParenteId,
                Boolean.TRUE.equals(dto.impedimento())
        );

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);

        return converterParaDTO(tarefaSalva);
    }

    @Transactional
    public void deletarTarefa(Long projetoId, Long tarefaId) {
        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);
        tarefaRepository.delete(tarefa);
    }

    @Transactional
    public List<TarefaResponseDTO> listarBacklog(Long projetoId, String status, String prioridade) {

        List<Tarefa> tarefas = tarefaRepository.findByProjetoIdAndSprintIdIsNull(projetoId);

        return tarefas.stream()
                .filter(t -> status == null || t.getStatus().name().equalsIgnoreCase(status))
                .filter(t -> prioridade == null || (t.getPrioridade() != null && t.getPrioridade().name().equalsIgnoreCase(prioridade)))
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public TarefaResponseDTO buscarTarefaPorId(Long projetoId, Long tarefaId) {
        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);
        return converterParaDTO(tarefa);
    }

    @Transactional
    public List<TarefaResponseDTO> listarTarefasDoProjeto(Long projetoId, String status, String prioridade, Long sprintId) {
        List<Tarefa> tarefas = tarefaRepository.findByProjetoId(projetoId);

        return tarefas.stream()
                .filter(t -> status == null || t.getStatus().name().equalsIgnoreCase(status))
                .filter(t -> prioridade == null ||
                        (t.getPrioridade() != null && t.getPrioridade().name().equalsIgnoreCase(prioridade)))
                .filter(t -> sprintId == null || Objects.equals(t.getSprintId(), sprintId))
                .map(this::converterParaDTO)
                .toList();
    }

    @Transactional
    public TarefaResponseDTO editarTarefa(Long projetoId, Long tarefaId, EditarTarefaRequestDTO dto) {

        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);

        if (dto.titulo() != null) {
            tarefa.setTitulo(dto.titulo());
        }

        if (dto.descricao() != null) {
            tarefa.setDescricao(dto.descricao());
        }

        if (dto.estimativa() != null) {
            tarefa.setEstimativa(dto.estimativa());
        }

        if (dto.prioridade() != null) {
            tarefa.setPrioridade(dto.prioridade());
        }

        return converterParaDTO(tarefaRepository.save(tarefa));
    }

    @Transactional
    public TarefaResponseDTO atualizarSprint(Long projetoId, Long tarefaId, AtualizarSprintTarefaRequestDTO dto) {
        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);

        Long sprintId = dto.sprintId();

        if (sprintId != null) {
            Sprint sprint = sprintRepository.findById(sprintId)
                    .orElseThrow(SprintNaoEncontradaException::new);

            if (!Objects.equals(sprint.getProjetoId(), projetoId)) {
                throw new SprintNaoPertenceAoProjetoException();
            }
        }

        tarefa.setSprintId(sprintId);

        return converterParaDTO(tarefaRepository.save(tarefa));
    }

    @Transactional
    public TarefaResponseDTO atualizarStatus(Long projetoId, Long tarefaId, AtualizarStatusTarefaRequestDTO dto) {

        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);

        tarefa.setStatus(dto.status());

        return converterParaDTO(tarefaRepository.save(tarefa));
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
                tarefa.getImpedimento()
        );
    }

    private Tarefa buscarTarefaDoProjeto(Long projetoId, Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(TarefaNaoEncontradaException::new);

        if (!Objects.equals(tarefa.getProjetoId(), projetoId)) {
            throw new TarefaNaoPertenceAoProjetoException();
        }

        return tarefa;
    }
}