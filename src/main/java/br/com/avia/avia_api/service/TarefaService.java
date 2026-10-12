package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.*;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.enums.StatusTarefa;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.sprint.SprintNaoEncontradaException;
import br.com.avia.avia_api.exception.sprint.SprintNaoPertenceAoProjetoException;
import br.com.avia.avia_api.exception.tarefa.*;
import br.com.avia.avia_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class TarefaService {

    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final TarefaRepository tarefaRepository;
    private final SprintRepository sprintRepository;
    private final TarefaProjetoUsuarioRepository tarefaProjetoUsuarioRepository;
    private final AnexoRepository anexoRepository;

    public TarefaService(
            UsuarioRepository usuarioRepository,
            ProjetoUsuarioRepository projetoUsuarioRepository,
            TarefaRepository tarefaRepository,
            SprintRepository sprintRepository,
            TarefaProjetoUsuarioRepository tarefaProjetoUsuarioRepository,
            AnexoRepository anexoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.tarefaRepository = tarefaRepository;
        this.sprintRepository = sprintRepository;
        this.tarefaProjetoUsuarioRepository = tarefaProjetoUsuarioRepository;
        this.anexoRepository = anexoRepository;
    }

    @Transactional
    public TarefaResponseDTO criarTarefa(Long projetoId, CriarTarefaRequestDTO dto, String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);

        Long sprintId = dto.sprintId();
        Long tarefaParenteId = dto.tarefaParenteId();

        if (tarefaParenteId != null) {
            verificarPermissao(projetoId, membro.getId(), "CRIARSUBTAREFAS", "CRIARTAREFAS");
        } else {
            verificarPermissao(projetoId, membro.getId(), "CRIARTAREFAS");
        }

        if (sprintId != null) {
            Sprint sprint = sprintRepository.findById(sprintId)
                    .orElseThrow(SprintNaoEncontradaException::new);
            if (!Objects.equals(sprint.getProjetoId(), projetoId)) {
                throw new SprintNaoPertenceAoProjetoException();
            }
        }

        if (tarefaParenteId != null) {
            Tarefa pai = buscarTarefaDoProjeto(projetoId, tarefaParenteId);
            validarPai(pai, null, dto.status());
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

        if (dto.tipo() != null) tarefa.setTipo(dto.tipo());

        Tarefa tarefaSalva = tarefaRepository.save(tarefa);

        if (dto.responsaveisIds() != null) {
            for (Long uid : dto.responsaveisIds()) {
                ProjetoUsuario pu = projetoUsuarioRepository.findByUsuarioIdAndProjetoId(uid, projetoId)
                        .orElseThrow(() -> new RuntimeException("Membro não encontrado no projeto."));
                tarefaProjetoUsuarioRepository.save(new TarefaProjetoUsuario(tarefaSalva.getId(), pu.getId()));
            }
        }

        return converterParaDTO(tarefaSalva);
    }

    @Transactional
    public void deletarTarefa(Long projetoId, Long tarefaId, String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);
        verificarPermissao(projetoId, membro.getId(), "EXCLUIRTAREFAS");
        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);
        if (tarefaRepository.existsByTarefaParenteId(tarefaId)) {
            throw new TarefaPossuiSubtarefasException();
        }
        tarefaProjetoUsuarioRepository.deleteByTarefaId(tarefaId);
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
    public List<TarefaResponseDTO> listarSubtarefas(Long projetoId, Long tarefaId) {
        buscarTarefaDoProjeto(projetoId, tarefaId);
        return tarefaRepository.findByTarefaParenteId(tarefaId).stream()
                .map(this::converterParaDTO)
                .toList();
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
    public TarefaResponseDTO editarTarefa(Long projetoId, Long tarefaId, EditarTarefaRequestDTO dto, String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);
        verificarPermissao(projetoId, membro.getId(), "EDITARTAREFAS");

        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);

        if (dto.titulo() != null) tarefa.setTitulo(dto.titulo());
        if (dto.descricao() != null) tarefa.setDescricao(dto.descricao());
        if (dto.estimativa() != null) tarefa.setEstimativa(dto.estimativa());
        if (dto.prioridade() != null) tarefa.setPrioridade(dto.prioridade());

        if (dto.responsaveisIds() != null) {
            tarefaProjetoUsuarioRepository.deleteByTarefaId(tarefaId);
            for (Long uid : dto.responsaveisIds()) {
                ProjetoUsuario pu = projetoUsuarioRepository.findByUsuarioIdAndProjetoId(uid, projetoId)
                        .orElseThrow(() -> new RuntimeException("Membro não encontrado no projeto."));
                tarefaProjetoUsuarioRepository.save(new TarefaProjetoUsuario(tarefaId, pu.getId()));
            }
        }

        if (dto.tarefaParenteId() != null) {
            if (dto.tarefaParenteId() == -1L) {
                tarefa.setTarefaParenteId(null);
            } else {
                Tarefa pai = buscarTarefaDoProjeto(projetoId, dto.tarefaParenteId());
                validarPai(pai, tarefaId, tarefa.getStatus());
                tarefa.setTarefaParenteId(pai.getId());
            }
        }

        if (dto.tipo() != null) tarefa.setTipo(dto.tipo());

        return converterParaDTO(tarefaRepository.save(tarefa));
    }

    @Transactional
    public TarefaResponseDTO atualizarSprint(Long projetoId, Long tarefaId, AtualizarSprintTarefaRequestDTO dto, String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);
        verificarPermissao(projetoId, membro.getId(), "MOVERTAREFAS");

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
    public TarefaResponseDTO atualizarStatus(Long projetoId, Long tarefaId, AtualizarStatusTarefaRequestDTO dto,
                                             String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);
        verificarPermissao(projetoId, membro.getId(), "MOVERTAREFAS");
        Tarefa tarefa = buscarTarefaDoProjeto(projetoId, tarefaId);
        validarTransicaoStatus(tarefa, dto.status());
        tarefa.setStatus(dto.status());
        return converterParaDTO(tarefaRepository.save(tarefa));
    }

    private TarefaResponseDTO converterParaDTO(Tarefa tarefa) {
        List<TarefaProjetoUsuario> tpus = tarefaProjetoUsuarioRepository.findByTarefaId(tarefa.getId());
        List<ResponsavelDTO> responsaveis = tpus.stream()
                .map(tpu -> projetoUsuarioRepository.findById(tpu.getProjetoUsuarioId()).orElse(null))
                .filter(Objects::nonNull)
                .map(pu -> new ResponsavelDTO(pu.getUsuario().getId(), pu.getUsuario().getNome()))
                .toList();

        int subtarefasCount = tarefaRepository.countByTarefaParenteId(tarefa.getId());
        int subtarefasConcluidasCount = tarefaRepository.countByTarefaParenteIdAndStatus(tarefa.getId(), StatusTarefa.CONCLUIDA);
        int anexosCount = anexoRepository.countByTarefaId(tarefa.getId());

        return new TarefaResponseDTO(
                tarefa.getId(),
                tarefa.getSprintId(),
                tarefa.getTitulo(),
                tarefa.getDescricao(),
                tarefa.getEstimativa(),
                tarefa.getStatus(),
                tarefa.getPrioridade(),
                tarefa.getTarefaParenteId(),
                tarefa.getImpedimento(),
                responsaveis,
                tarefa.getTipo(),
                subtarefasCount,
                subtarefasConcluidasCount,
                anexosCount
        );
    }

    private void validarPai(Tarefa pai, Long filhoId, StatusTarefa statusFilho) {
        if (Objects.equals(pai.getId(), filhoId)) {
            throw new HierarquiaTarefaInvalidaException("Uma tarefa não pode ser subtarefa de si mesma");
        }
        if (pai.getTarefaParenteId() != null) {
            throw new HierarquiaTarefaInvalidaException("Não é possível criar subtarefa de uma subtarefa");
        }
        if (filhoId != null && tarefaRepository.existsByTarefaParenteId(filhoId)) {
            throw new HierarquiaTarefaInvalidaException("Uma tarefa que possui subtarefas não pode se tornar subtarefa");
        }
        if (pai.getStatus() == StatusTarefa.CONCLUIDA && statusFilho != StatusTarefa.CONCLUIDA) {
            throw new TarefaPaiConcluidaException();
        }
    }

    private void validarTransicaoStatus(Tarefa tarefa, StatusTarefa novoStatus) {
        boolean concluindo = novoStatus == StatusTarefa.CONCLUIDA;
        if (concluindo && tarefaRepository.existsByTarefaParenteIdAndStatusNot(tarefa.getId(), StatusTarefa.CONCLUIDA)) {
            throw new SubtarefasPendentesException();
        }

        boolean reabrindo = tarefa.getStatus() == StatusTarefa.CONCLUIDA && !concluindo;
        if (reabrindo && tarefa.getTarefaParenteId() != null) {
            boolean paiConcluido = tarefaRepository.findById(tarefa.getTarefaParenteId())
                    .map(pai -> pai.getStatus() == StatusTarefa.CONCLUIDA)
                    .orElse(false);
            if (paiConcluido) {
                throw new TarefaPaiConcluidaException();
            }
        }
    }

    private Tarefa buscarTarefaDoProjeto(Long projetoId, Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(TarefaNaoEncontradaException::new);
        if (!Objects.equals(tarefa.getProjetoId(), projetoId)) {
            throw new TarefaNaoPertenceAoProjetoException();
        }
        return tarefa;
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));
    }

    private void verificarPermissao(Long projetoId, Long usuarioId, String... permissoesAceitas) {
        ProjetoUsuario membro = projetoUsuarioRepository.findByUsuarioIdAndProjetoId(usuarioId, projetoId)
                .orElseThrow(() -> new RuntimeException("Usuário não é membro do projeto."));

        List<Permissao> permissoes = membro.getPermissoes().isEmpty()
                ? new ArrayList<>(membro.getPapel().getPermissoes())
                : membro.getPermissoes();

        List<String> aceitas = List.of(permissoesAceitas);
        boolean possuiPermissao = permissoes.stream()
                .anyMatch(p -> aceitas.contains(p.getDescricao()));

        if (!possuiPermissao)
            throw new PermissaoNegadaException();
    }
}
