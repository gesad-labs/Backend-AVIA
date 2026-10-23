package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.*;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.enums.StatusTarefa;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.sprint.IntervaloSprintsInvalidoException;
import br.com.avia.avia_api.exception.sprint.SprintNaoEncontradaException;
import br.com.avia.avia_api.exception.sprint.SprintNaoPertenceAoProjetoException;
import br.com.avia.avia_api.exception.tarefa.*;
import br.com.avia.avia_api.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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

        if (dto.bloqueadaPorId() != null) {
            tarefa.setBloqueadaPorId(dto.bloqueadaPorId());
            Tarefa predecessora = validarBloqueio(projetoId, tarefa);
            if (dto.status() == StatusTarefa.FAZENDO && predecessora.getStatus() != StatusTarefa.CONCLUIDA) {
                throw new TarefaBloqueadaException(predecessora.getTitulo());
            }
        }

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
        List<Tarefa> bloqueadasPorEla = tarefaRepository.findByBloqueadaPorId(tarefaId);
        bloqueadasPorEla.forEach(t -> t.setBloqueadaPorId(null));
        tarefaRepository.saveAll(bloqueadasPorEla);
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
    public List<TarefaResponseDTO> listarSubtarefas(Long projetoId, Long tarefaId, String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);
        verificarPermissao(projetoId, membro.getId(), "VISUALIZARPROJETO");

        Tarefa pai = tarefaRepository.findByIdAndProjetoId(tarefaId, projetoId)
                .orElseThrow(TarefaNaoEncontradaException::new);

        return tarefaRepository.findByTarefaParenteId(pai.getId()).stream()
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

    // Filtros opcionais, combinados com E; nos de múltipla escolha basta casar com um dos valores.
    // Como a Sprint não tem datas, o intervalo segue a ordem de criação (id) das sprints do projeto.
    @Transactional
    public List<TarefaResponseDTO> filtrarHistorico(Long projetoId, FiltroHistoricoDTO filtro, String emailMembro) {
        Usuario membro = buscarUsuarioPorEmail(emailMembro);
        verificarPermissao(projetoId, membro.getId(), "VISUALIZARPROJETO");

        Long sprintInicioId = filtro.sprintInicioId();
        Long sprintFimId = filtro.sprintFimId();
        if (sprintInicioId != null) validarSprintDoProjeto(projetoId, sprintInicioId);
        if (sprintFimId != null) validarSprintDoProjeto(projetoId, sprintFimId);
        if (sprintInicioId != null && sprintFimId != null && sprintInicioId > sprintFimId) {
            throw new IntervaloSprintsInvalidoException();
        }

        List<StatusTarefa> status = filtro.status();
        List<Long> responsaveisIds = filtro.responsaveisIds();
        Boolean impedimento = filtro.impedimento();

        return tarefaRepository.findByProjetoId(projetoId).stream()
                .filter(t -> sprintInicioId == null || (t.getSprintId() != null && t.getSprintId() >= sprintInicioId))
                .filter(t -> sprintFimId == null || (t.getSprintId() != null && t.getSprintId() <= sprintFimId))
                .filter(t -> status == null || status.isEmpty() || status.contains(t.getStatus()))
                .filter(t -> impedimento == null || impedimento.equals(Boolean.TRUE.equals(t.getImpedimento())))
                .map(this::converterParaDTO)
                .filter(t -> responsaveisIds == null || responsaveisIds.isEmpty()
                        || t.responsaveis().stream().anyMatch(r -> responsaveisIds.contains(r.id())))
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

        if (dto.bloqueadaPorId() != null) {
            tarefa.setBloqueadaPorId(dto.bloqueadaPorId() == -1L ? null : dto.bloqueadaPorId());
        }

        boolean dependenciaAlterada = dto.bloqueadaPorId() != null || dto.tarefaParenteId() != null;
        if (dependenciaAlterada && tarefa.getBloqueadaPorId() != null) {
            validarBloqueio(projetoId, tarefa);
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
                tarefa.getBloqueadaPorId(),
                buscarPredecessoraPendente(tarefa).isPresent(),
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
        if (filhoId != null && Objects.equals(pai.getBloqueadaPorId(), filhoId)) {
            throw new DependenciaInvalidaException("Uma tarefa não pode se tornar subtarefa de uma tarefa que ela bloqueia");
        }
        if (pai.getStatus() == StatusTarefa.CONCLUIDA && statusFilho != StatusTarefa.CONCLUIDA) {
            throw new TarefaPaiConcluidaException();
        }
    }

    private void validarTransicaoStatus(Tarefa tarefa, StatusTarefa novoStatus) {
        if (novoStatus == StatusTarefa.FAZENDO && tarefa.getStatus() != StatusTarefa.FAZENDO) {
            buscarPredecessoraPendente(tarefa).ifPresent(predecessora -> {
                throw new TarefaBloqueadaException(predecessora.getTitulo());
            });
        }

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

    private Optional<Tarefa> buscarPredecessoraPendente(Tarefa tarefa) {
        if (tarefa.getBloqueadaPorId() == null) {
            return Optional.empty();
        }
        return tarefaRepository.findById(tarefa.getBloqueadaPorId())
                .filter(predecessora -> predecessora.getStatus() != StatusTarefa.CONCLUIDA);
    }

    private Tarefa validarBloqueio(Long projetoId, Tarefa tarefa) {
        Tarefa predecessora = buscarTarefaDoProjeto(projetoId, tarefa.getBloqueadaPorId());
        if (Objects.equals(predecessora.getId(), tarefa.getId())) {
            throw new DependenciaInvalidaException("Uma tarefa não pode ser bloqueada por si mesma");
        }
        boolean predecessoraEhPai = Objects.equals(predecessora.getId(), tarefa.getTarefaParenteId());
        boolean predecessoraEhSubtarefa = tarefa.getId() != null
                && Objects.equals(predecessora.getTarefaParenteId(), tarefa.getId());
        if (predecessoraEhPai || predecessoraEhSubtarefa) {
            throw new DependenciaInvalidaException("Uma tarefa não pode ser bloqueada pela própria tarefa pai nem por uma de suas subtarefas");
        }
        if (formaCiclo(predecessora, tarefa)) {
            throw new DependenciaCircularException();
        }
        return predecessora;
    }

    // Percorre tudo o que precisa ser concluído antes da predecessora (quem a bloqueia e suas subtarefas).
    // Se a própria tarefa ou a tarefa pai dela estiver nesse caminho, nenhuma delas consegue terminar.
    private boolean formaCiclo(Tarefa predecessora, Tarefa tarefa) {
        Set<Long> alvos = new HashSet<>();
        if (tarefa.getId() != null) alvos.add(tarefa.getId());
        if (tarefa.getTarefaParenteId() != null) alvos.add(tarefa.getTarefaParenteId());

        Deque<Tarefa> pendentes = new ArrayDeque<>();
        pendentes.push(predecessora);
        Set<Long> visitadas = new HashSet<>();

        while (!pendentes.isEmpty()) {
            Tarefa atual = pendentes.pop();
            if (!visitadas.add(atual.getId())) continue;
            if (alvos.contains(atual.getId())) return true;

            if (atual.getBloqueadaPorId() != null) {
                tarefaRepository.findById(atual.getBloqueadaPorId()).ifPresent(pendentes::push);
            }
            tarefaRepository.findByTarefaParenteId(atual.getId()).forEach(pendentes::push);
        }
        return false;
    }

    private Tarefa buscarTarefaDoProjeto(Long projetoId, Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(TarefaNaoEncontradaException::new);
        if (!Objects.equals(tarefa.getProjetoId(), projetoId)) {
            throw new TarefaNaoPertenceAoProjetoException();
        }
        return tarefa;
    }

    private void validarSprintDoProjeto(Long projetoId, Long sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(SprintNaoEncontradaException::new);
        if (!Objects.equals(sprint.getProjetoId(), projetoId)) {
            throw new SprintNaoPertenceAoProjetoException();
        }
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
