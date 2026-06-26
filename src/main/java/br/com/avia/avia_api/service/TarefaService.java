package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.*;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final SprintRepository sprintRepository;
    private final AnexoRepository anexoRepository;

    public TarefaService(
            TarefaRepository tarefaRepository,
            SprintRepository sprintRepository,
            AnexoRepository anexoRepository
    ) {
        this.tarefaRepository = tarefaRepository;
        this.sprintRepository = sprintRepository;
        this.anexoRepository = anexoRepository;
    }

    @Transactional
    public TarefaResponseDTO criarTarefa(Long projetoId, Long sprintId, CriarTarefaRequestDTO dto) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new RuntimeException("Sprint não encontrada"));

        if (sprint.getProjetoId() != projetoId) {
            throw new RuntimeException("Sprint não pertence a esse projeto");
        }

        Long tarefaParenteId = dto.tarefaParenteId();

        if (tarefaParenteId != null) {
            Tarefa tarefaParente = tarefaRepository.findById(tarefaParenteId)
                    .orElseThrow(() -> new RuntimeException("Tarefa parente não encontrada"));

            if (tarefaParente.getProjetoId() != projetoId) {
                throw new RuntimeException("Tarefa parente não pertence a esse projeto");
            }
        }

        Tarefa tarefa = new Tarefa(
                projetoId,
                sprintId,
                dto.titulo(),
                dto.descricao(),
                dto.estimativa(),
                "PENDENTE",
                dto.prioridade().toUpperCase(),
                tarefaParenteId,
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

        Long tarefaParenteId = dto.tarefaParenteId();

        if (tarefaParenteId != null) {
            Tarefa tarefaParente = tarefaRepository.findById(tarefaParenteId)
                    .orElseThrow(() -> new RuntimeException("Tarefa parente não encontrada"));

            if (tarefaParente.getProjetoId() != projetoId) {
                throw new RuntimeException("Tarefa parente não pertence a esse projeto");
            }
        }

        Tarefa tarefa = new Tarefa(
                projetoId,
                0,
                dto.titulo(),
                dto.descricao(),
                0,
                "BACKLOG",
                dto.prioridade().toUpperCase(),
                tarefaParenteId,
                false
        );

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);

        return converterParaDTO(tarefaSalva);
    }

    @Transactional
    public List<TarefaResponseDTO> listarBacklog(Long projetoId, String status, String prioridade) {

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

    @Transactional
    public TarefaResponseDTO editarTarefa(Long projetoId, Long tarefaId, EditarTarefaRequestDTO dto) {

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (tarefa.getProjetoId() != projetoId) {
            throw new RuntimeException("Tarefa não pertence a esse projeto");
        }

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
            tarefa.setPrioridade(dto.prioridade().toUpperCase());
        }

        Tarefa tarefaAtualizada = tarefaRepository.save(tarefa);

        return converterParaDTO(tarefaAtualizada);
    }

    @Transactional
    public TarefaResponseDTO atualizarStatus(Long projetoId, Long tarefaId, AtualizarStatusTarefaRequestDTO dto) {

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (tarefa.getProjetoId() != projetoId) {
            throw new RuntimeException("Tarefa não pertence a esse projeto");
        }

        tarefa.setStatus(dto.status().toUpperCase());

        Tarefa tarefaAtualizada = tarefaRepository.save(tarefa);

        return converterParaDTO(tarefaAtualizada);
    }

    @Transactional
    public AnexoResponseDTO adicionarAnexo(
            Long projetoId,
            Long tarefaId,
            CriarAnexoRequestDTO dto
    ) {

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (tarefa.getProjetoId() != projetoId) {
            throw new RuntimeException("Tarefa não pertence a esse projeto");
        }

        Anexo anexo = new Anexo(
                tarefaId,
                dto.nome(),
                dto.tipoArquivo(),
                dto.url()
        );

        Anexo salvo = anexoRepository.save(anexo);

        return new AnexoResponseDTO(
                salvo.getId(),
                salvo.getTarefaId(),
                salvo.getNome(),
                salvo.getTipoArquivo(),
                salvo.getUrl()
        );
    }

    @Transactional
    public void removerAnexo(
            Long projetoId,
            Long tarefaId,
            Integer anexoId
    ) {

        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(() -> new RuntimeException("Tarefa não encontrada"));

        if (tarefa.getProjetoId() != projetoId) {
            throw new RuntimeException("Tarefa não pertence a esse projeto");
        }

        Anexo anexo = anexoRepository.findById(anexoId)
                .orElseThrow(() -> new RuntimeException("Anexo não encontrado"));

        if (anexo.getTarefaId() != tarefaId) {
            throw new RuntimeException("Anexo não pertence a essa tarefa");
        }

        anexoRepository.delete(anexo);
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