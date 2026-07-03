package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.planningPoker.ParticipantePlanningPokerResponseDTO;
import br.com.avia.avia_api.dto.planningPoker.PlanningPokerSessaoResponseDTO;
import br.com.avia.avia_api.entity.Papel;
import br.com.avia.avia_api.entity.PlanningPokerSessao;
import br.com.avia.avia_api.entity.PlanningPokerVoto;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.enums.CargoProjeto;
import br.com.avia.avia_api.enums.StatusPlanningPokerSessao;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.planningPoker.PlanningPokerException;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoEncontradaException;
import br.com.avia.avia_api.exception.tarefa.TarefaNaoPertenceAoProjetoException;
import br.com.avia.avia_api.repository.PlanningPokerSessaoRepository;
import br.com.avia.avia_api.repository.PlanningPokerVotoRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PlanningPokerService {

    private static final Set<StatusPlanningPokerSessao> STATUS_ATIVOS = Set.of(
            StatusPlanningPokerSessao.VOTANDO,
            StatusPlanningPokerSessao.REVELADA
    );

    private final PlanningPokerSessaoRepository sessaoRepository;
    private final PlanningPokerVotoRepository votoRepository;
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;

    public PlanningPokerService(
            PlanningPokerSessaoRepository sessaoRepository,
            PlanningPokerVotoRepository votoRepository,
            TarefaRepository tarefaRepository,
            UsuarioRepository usuarioRepository,
            ProjetoUsuarioRepository projetoUsuarioRepository
    ) {
        this.sessaoRepository = sessaoRepository;
        this.votoRepository = votoRepository;
        this.tarefaRepository = tarefaRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
    }

    @Transactional
    public PlanningPokerSessaoResponseDTO criarSessao(Long projetoId, Long tarefaId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        validarGerente(projetoId, usuario);
        buscarTarefaDoProjeto(projetoId, tarefaId);

        Optional<PlanningPokerSessao> sessaoAtiva =
                sessaoRepository.findFirstByProjetoIdAndTarefaIdAndStatusInOrderByCriadoEmDesc(
                        projetoId,
                        tarefaId,
                        STATUS_ATIVOS
                );

        if (sessaoAtiva.isPresent()) {
            throw new PlanningPokerException("Ja existe uma sessao ativa de planning poker para esta tarefa.");
        }

        PlanningPokerSessao sessao = new PlanningPokerSessao(projetoId, tarefaId, usuario.getId());
        PlanningPokerSessao sessaoSalva = sessaoRepository.save(sessao);

        return converterParaResponse(sessaoSalva);
    }

    @Transactional(readOnly = true)
    public List<Long> listarTarefasComSessaoAtiva(Long projetoId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        validarMembro(projetoId, usuario);
        return sessaoRepository.findByProjetoIdAndStatusIn(projetoId, STATUS_ATIVOS)
                .stream()
                .map(PlanningPokerSessao::getTarefaId)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<PlanningPokerSessaoResponseDTO> buscarSessaoAtiva(
            Long projetoId,
            Long tarefaId,
            String emailUsuario
    ) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        validarMembro(projetoId, usuario);
        buscarTarefaDoProjeto(projetoId, tarefaId);

        return sessaoRepository.findFirstByProjetoIdAndTarefaIdAndStatusInOrderByCriadoEmDesc(
                        projetoId,
                        tarefaId,
                        STATUS_ATIVOS
                )
                .map(this::converterParaResponse);
    }

    @Transactional
    public PlanningPokerSessaoResponseDTO votar(Long sessaoId, Integer valor, String emailUsuario) {
        if (valor == null || valor < 0) {
            throw new PlanningPokerException("O voto deve ser um valor maior ou igual a zero.");
        }

        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        PlanningPokerSessao sessao = buscarSessao(sessaoId);
        validarMembro(sessao.getProjetoId(), usuario);

        if (!StatusPlanningPokerSessao.VOTANDO.equals(sessao.getStatus())) {
            throw new PlanningPokerException("A sessao nao esta aberta para votos.");
        }

        PlanningPokerVoto voto = votoRepository
                .findBySessaoIdAndUsuarioIdAndRodada(sessao.getId(), usuario.getId(), sessao.getRodadaAtual())
                .orElseGet(() -> new PlanningPokerVoto(sessao, usuario, sessao.getRodadaAtual(), valor));

        voto.atualizarValor(valor);
        votoRepository.save(voto);

        return converterParaResponse(sessao);
    }

    @Transactional
    public PlanningPokerSessaoResponseDTO revelar(Long sessaoId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        PlanningPokerSessao sessao = buscarSessao(sessaoId);
        validarGerente(sessao.getProjetoId(), usuario);

        if (!StatusPlanningPokerSessao.VOTANDO.equals(sessao.getStatus())) {
            throw new PlanningPokerException("A sessao nao esta em votacao.");
        }

        sessao.setStatus(StatusPlanningPokerSessao.REVELADA);
        sessao.setReveladoEm(LocalDateTime.now());

        return converterParaResponse(sessaoRepository.save(sessao));
    }

    @Transactional
    public PlanningPokerSessaoResponseDTO novaRodada(Long sessaoId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        PlanningPokerSessao sessao = buscarSessao(sessaoId);
        validarGerente(sessao.getProjetoId(), usuario);

        if (!StatusPlanningPokerSessao.REVELADA.equals(sessao.getStatus())) {
            throw new PlanningPokerException("So e possivel iniciar nova rodada depois de revelar os votos.");
        }

        sessao.setStatus(StatusPlanningPokerSessao.VOTANDO);
        sessao.setRodadaAtual(sessao.getRodadaAtual() + 1);
        sessao.setReveladoEm(null);

        return converterParaResponse(sessaoRepository.save(sessao));
    }

    @Transactional
    public PlanningPokerSessaoResponseDTO finalizar(Long sessaoId, Integer estimativaFinal, String emailUsuario) {
        if (estimativaFinal == null || estimativaFinal < 0) {
            throw new PlanningPokerException("A estimativa final deve ser maior ou igual a zero.");
        }

        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        PlanningPokerSessao sessao = buscarSessao(sessaoId);
        validarGerente(sessao.getProjetoId(), usuario);

        if (!StatusPlanningPokerSessao.REVELADA.equals(sessao.getStatus())) {
            throw new PlanningPokerException("So e possivel finalizar depois de revelar os votos.");
        }

        Tarefa tarefa = buscarTarefaDoProjeto(sessao.getProjetoId(), sessao.getTarefaId());
        tarefa.setEstimativa(estimativaFinal);
        tarefaRepository.save(tarefa);

        sessao.setStatus(StatusPlanningPokerSessao.FINALIZADA);
        sessao.setEstimativaFinal(estimativaFinal);
        sessao.setFinalizadoEm(LocalDateTime.now());

        return converterParaResponse(sessaoRepository.save(sessao));
    }

    @Transactional
    public PlanningPokerSessaoResponseDTO cancelar(Long sessaoId, String emailUsuario) {
        Usuario usuario = buscarUsuarioPorEmail(emailUsuario);
        PlanningPokerSessao sessao = buscarSessao(sessaoId);
        validarGerente(sessao.getProjetoId(), usuario);

        if (StatusPlanningPokerSessao.FINALIZADA.equals(sessao.getStatus())) {
            throw new PlanningPokerException("Nao e possivel cancelar uma sessao finalizada.");
        }

        sessao.setStatus(StatusPlanningPokerSessao.CANCELADA);

        return converterParaResponse(sessaoRepository.save(sessao));
    }

    private PlanningPokerSessao buscarSessao(Long sessaoId) {
        return sessaoRepository.findById(sessaoId)
                .orElseThrow(() -> new PlanningPokerException("Sessao de planning poker nao encontrada."));
    }

    private Usuario buscarUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new PlanningPokerException("Usuario autenticado nao encontrado."));
    }

    private Tarefa buscarTarefaDoProjeto(Long projetoId, Long tarefaId) {
        Tarefa tarefa = tarefaRepository.findById(tarefaId)
                .orElseThrow(TarefaNaoEncontradaException::new);

        if (!Objects.equals(tarefa.getProjetoId(), projetoId)) {
            throw new TarefaNaoPertenceAoProjetoException();
        }

        return tarefa;
    }

    private ProjetoUsuario buscarVinculo(Long projetoId, Usuario usuario) {
        return projetoUsuarioRepository.findByProjetoIdAndUsuarioId(projetoId, usuario.getId())
                .orElseThrow(PermissaoNegadaException::new);
    }

    private void validarMembro(Long projetoId, Usuario usuario) {
        buscarVinculo(projetoId, usuario);
    }

    private void validarGerente(Long projetoId, Usuario usuario) {
        ProjetoUsuario vinculo = buscarVinculo(projetoId, usuario);

        boolean cargoGerente = CargoProjeto.GERENTE.equals(vinculo.getCargo());

        Papel papel = vinculo.getPapel();
        boolean papelGerente = papel != null && "GERENTE".equalsIgnoreCase(papel.getNome());

        if (!cargoGerente && !papelGerente) {
            throw new PermissaoNegadaException();
        }
    }

    private PlanningPokerSessaoResponseDTO converterParaResponse(PlanningPokerSessao sessao) {
        List<PlanningPokerVoto> votos =
                votoRepository.findBySessaoIdAndRodada(sessao.getId(), sessao.getRodadaAtual());

        Map<Long, PlanningPokerVoto> votoPorUsuario = votos.stream()
                .collect(Collectors.toMap(v -> v.getUsuario().getId(), Function.identity()));

        boolean mostrarValores = StatusPlanningPokerSessao.REVELADA.equals(sessao.getStatus())
                || StatusPlanningPokerSessao.FINALIZADA.equals(sessao.getStatus());

        List<ParticipantePlanningPokerResponseDTO> participantes =
                projetoUsuarioRepository.findMembrosByProjetoId(sessao.getProjetoId())
                        .stream()
                        .map(membro -> {
                            Usuario usuario = membro.getUsuario();
                            PlanningPokerVoto voto = votoPorUsuario.get(usuario.getId());

                            return new ParticipantePlanningPokerResponseDTO(
                                    usuario.getId(),
                                    usuario.getNome(),
                                    voto != null,
                                    mostrarValores && voto != null ? voto.getValor() : null
                            );
                        })
                        .toList();

        return new PlanningPokerSessaoResponseDTO(
                sessao.getId(),
                sessao.getProjetoId(),
                sessao.getTarefaId(),
                sessao.getGerenteId(),
                sessao.getStatus(),
                sessao.getRodadaAtual(),
                sessao.getEstimativaFinal(),
                participantes
        );
    }
}