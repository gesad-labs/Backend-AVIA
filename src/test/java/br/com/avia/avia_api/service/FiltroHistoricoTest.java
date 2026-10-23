package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.FiltroHistoricoDTO;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.entity.*;
import br.com.avia.avia_api.enums.StatusSprint;
import br.com.avia.avia_api.enums.StatusTarefa;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.sprint.IntervaloSprintsInvalidoException;
import br.com.avia.avia_api.exception.sprint.SprintNaoPertenceAoProjetoException;
import br.com.avia.avia_api.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FiltroHistoricoTest {

    private static final Long PROJETO_ID = 1L;
    private static final Long USUARIO_ID = 10L;
    private static final String EMAIL = "membro@avia.com";

    private static final Long ANA = 20L;
    private static final Long BRUNO = 21L;
    private static final Long CARLA = 22L;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private SprintRepository sprintRepository;
    @Mock private TarefaProjetoUsuarioRepository tarefaProjetoUsuarioRepository;
    @Mock private AnexoRepository anexoRepository;

    @InjectMocks
    private TarefaService tarefaService;

    private final List<Tarefa> tarefasDoProjeto = new ArrayList<>();
    private final Map<Long, Long> projetoUsuarioIdPorUsuario = new HashMap<>();

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario("Membro", EMAIL, "senha");
        ReflectionTestUtils.setField(usuario, "id", USUARIO_ID);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(tarefaRepository.findByProjetoId(PROJETO_ID)).thenReturn(tarefasDoProjeto);
        comPermissoes("VISUALIZARPROJETO");

        membroDoProjeto(ANA, 200L);
        membroDoProjeto(BRUNO, 201L);
        membroDoProjeto(CARLA, 202L);

        sprint(30L, PROJETO_ID);
        sprint(31L, PROJETO_ID);
        sprint(32L, PROJETO_ID);
        sprint(33L, PROJETO_ID);
    }

    @Test
    void semFiltrosRetornaTodasAsTarefasDoProjeto() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, false);
        tarefa(101L, null, StatusTarefa.CONCLUIDA, true);

        assertThat(filtrar(new FiltroHistoricoDTO(null, null, null, null, null)))
                .containsExactly(100L, 101L);
    }

    @Test
    void filtraPorVariosResponsaveisAoMesmoTempo() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, false, ANA);
        tarefa(101L, 30L, StatusTarefa.A_FAZER, false, BRUNO);
        tarefa(102L, 30L, StatusTarefa.A_FAZER, false, CARLA);
        tarefa(103L, 30L, StatusTarefa.A_FAZER, false, CARLA, ANA);
        tarefa(104L, 30L, StatusTarefa.A_FAZER, false);

        assertThat(filtrar(new FiltroHistoricoDTO(List.of(ANA, BRUNO), null, null, null, null)))
                .containsExactly(100L, 101L, 103L);
    }

    @Test
    void filtraPorIntervaloDeSprintsIgnorandoBacklog() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, false);
        tarefa(101L, 31L, StatusTarefa.A_FAZER, false);
        tarefa(102L, 32L, StatusTarefa.A_FAZER, false);
        tarefa(103L, 33L, StatusTarefa.A_FAZER, false);
        tarefa(104L, null, StatusTarefa.A_FAZER, false);

        assertThat(filtrar(new FiltroHistoricoDTO(null, 31L, 32L, null, null)))
                .containsExactly(101L, 102L);
    }

    @Test
    void intervaloAbertoSoComSprintInicialOuFinal() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, false);
        tarefa(101L, 31L, StatusTarefa.A_FAZER, false);
        tarefa(102L, 32L, StatusTarefa.A_FAZER, false);

        assertThat(filtrar(new FiltroHistoricoDTO(null, 31L, null, null, null)))
                .containsExactly(101L, 102L);
        assertThat(filtrar(new FiltroHistoricoDTO(null, null, 31L, null, null)))
                .containsExactly(100L, 101L);
    }

    @Test
    void intervaloDeUmaSprintSo() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, false);
        tarefa(101L, 31L, StatusTarefa.A_FAZER, false);

        assertThat(filtrar(new FiltroHistoricoDTO(null, 31L, 31L, null, null)))
                .containsExactly(101L);
    }

    @Test
    void filtraPorVariosStatus() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, false);
        tarefa(101L, 30L, StatusTarefa.FAZENDO, false);
        tarefa(102L, 30L, StatusTarefa.TESTE, false);
        tarefa(103L, 30L, StatusTarefa.CONCLUIDA, false);

        assertThat(filtrar(new FiltroHistoricoDTO(null, null, null, List.of(StatusTarefa.FAZENDO, StatusTarefa.TESTE), null)))
                .containsExactly(101L, 102L);
    }

    @Test
    void filtraPorPresencaOuAusenciaDeImpedimento() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, true);
        tarefa(101L, 30L, StatusTarefa.A_FAZER, false);
        Tarefa semFlag = tarefa(102L, 30L, StatusTarefa.A_FAZER, false);
        semFlag.setImpedimento(null);

        assertThat(filtrar(new FiltroHistoricoDTO(null, null, null, null, true)))
                .containsExactly(100L);
        assertThat(filtrar(new FiltroHistoricoDTO(null, null, null, null, false)))
                .containsExactly(101L, 102L);
    }

    @Test
    void combinaTodosOsFiltrosAoMesmoTempo() {
        tarefa(100L, 31L, StatusTarefa.FAZENDO, true, ANA);    // passa em todos
        tarefa(101L, 31L, StatusTarefa.FAZENDO, true, CARLA);  // responsável fora
        tarefa(102L, 30L, StatusTarefa.FAZENDO, true, BRUNO);  // sprint fora do intervalo
        tarefa(103L, 32L, StatusTarefa.CONCLUIDA, true, ANA);  // status fora
        tarefa(104L, 32L, StatusTarefa.TESTE, false, BRUNO);   // sem impedimento
        tarefa(105L, 32L, StatusTarefa.TESTE, true, BRUNO);    // passa em todos

        FiltroHistoricoDTO filtro = new FiltroHistoricoDTO(
                List.of(ANA, BRUNO), 31L, 32L, List.of(StatusTarefa.FAZENDO, StatusTarefa.TESTE), true);

        assertThat(filtrar(filtro)).containsExactly(100L, 105L);
    }

    @Test
    void listasVaziasNaoFiltram() {
        tarefa(100L, 30L, StatusTarefa.A_FAZER, false, ANA);
        tarefa(101L, null, StatusTarefa.CONCLUIDA, false);

        assertThat(filtrar(new FiltroHistoricoDTO(List.of(), null, null, List.of(), null)))
                .containsExactly(100L, 101L);
    }

    @Test
    void naoAceitaIntervaloComSprintInicialDepoisDaFinal() {
        assertThatThrownBy(() -> filtrar(new FiltroHistoricoDTO(null, 32L, 31L, null, null)))
                .isInstanceOf(IntervaloSprintsInvalidoException.class);
    }

    @Test
    void naoAceitaSprintDeOutroProjeto() {
        sprint(90L, 2L);

        assertThatThrownBy(() -> filtrar(new FiltroHistoricoDTO(null, 90L, null, null, null)))
                .isInstanceOf(SprintNaoPertenceAoProjetoException.class);
    }

    @Test
    void semPermissaoDeVisualizarNaoFiltra() {
        comPermissoes("EDITARTAREFAS");

        assertThatThrownBy(() -> filtrar(new FiltroHistoricoDTO(null, null, null, null, null)))
                .isInstanceOf(PermissaoNegadaException.class);
    }

    @Test
    void quemNaoEhMembroNaoFiltra() {
        when(projetoUsuarioRepository.findByUsuarioIdAndProjetoId(USUARIO_ID, PROJETO_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> filtrar(new FiltroHistoricoDTO(null, null, null, null, null)))
                .hasMessage("Usuário não é membro do projeto.");
    }

    // ---- Auxiliares ----

    private List<Long> filtrar(FiltroHistoricoDTO filtro) {
        return tarefaService.filtrarHistorico(PROJETO_ID, filtro, EMAIL).stream()
                .map(TarefaResponseDTO::id)
                .toList();
    }

    private void comPermissoes(String... descricoes) {
        ProjetoUsuario membro = new ProjetoUsuario();
        membro.setPermissoes(Arrays.stream(descricoes).map(Permissao::new).toList());
        when(projetoUsuarioRepository.findByUsuarioIdAndProjetoId(USUARIO_ID, PROJETO_ID))
                .thenReturn(Optional.of(membro));
    }

    private void membroDoProjeto(Long usuarioId, Long projetoUsuarioId) {
        Usuario usuario = new Usuario("Usuario " + usuarioId, usuarioId + "@avia.com", "senha");
        ReflectionTestUtils.setField(usuario, "id", usuarioId);
        ProjetoUsuario pu = new ProjetoUsuario();
        ReflectionTestUtils.setField(pu, "id", projetoUsuarioId);
        ReflectionTestUtils.setField(pu, "usuario", usuario);
        when(projetoUsuarioRepository.findById(projetoUsuarioId)).thenReturn(Optional.of(pu));
        projetoUsuarioIdPorUsuario.put(usuarioId, projetoUsuarioId);
    }

    private void sprint(Long id, Long projetoId) {
        Sprint sprint = new Sprint(projetoId, "Sprint " + id, StatusSprint.EM_PLANEJAMENTO);
        ReflectionTestUtils.setField(sprint, "id", id);
        when(sprintRepository.findById(id)).thenReturn(Optional.of(sprint));
    }

    private Tarefa tarefa(Long id, Long sprintId, StatusTarefa status, boolean impedimento, Long... responsaveis) {
        Tarefa tarefa = new Tarefa(PROJETO_ID, sprintId, "Tarefa " + id, "desc", null, status, null, null, impedimento);
        ReflectionTestUtils.setField(tarefa, "id", id);
        tarefasDoProjeto.add(tarefa);

        List<TarefaProjetoUsuario> vinculos = Arrays.stream(responsaveis)
                .map(usuarioId -> new TarefaProjetoUsuario(id, projetoUsuarioIdPorUsuario.get(usuarioId)))
                .toList();
        when(tarefaProjetoUsuarioRepository.findByTarefaId(id)).thenReturn(vinculos);
        return tarefa;
    }
}
