package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.AtualizarStatusTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.CriarTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.EditarTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.entity.Permissao;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.enums.StatusTarefa;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.exception.tarefa.*;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TarefaServiceTest {

    private static final Long PROJETO_ID = 1L;
    private static final Long USUARIO_ID = 10L;
    private static final String EMAIL = "membro@avia.com";

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private SprintRepository sprintRepository;
    @Mock private TarefaProjetoUsuarioRepository tarefaProjetoUsuarioRepository;
    @Mock private AnexoRepository anexoRepository;

    @InjectMocks
    private TarefaService tarefaService;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario("Membro", EMAIL, "senha");
        ReflectionTestUtils.setField(usuario, "id", USUARIO_ID);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(inv -> {
            Tarefa tarefa = inv.getArgument(0);
            if (tarefa.getId() == null) ReflectionTestUtils.setField(tarefa, "id", 500L);
            return tarefa;
        });
        comPermissoes("CRIARTAREFAS", "CRIARSUBTAREFAS", "EDITARTAREFAS", "MOVERTAREFAS", "EXCLUIRTAREFAS");
    }

    // ---- Permissões de criação ----

    @Test
    void membroSemCriarTarefasNaoCriaTarefaRaiz() {
        comPermissoes("CRIARSUBTAREFAS");

        assertThatThrownBy(() -> tarefaService.criarTarefa(PROJETO_ID, criarDTO(null, StatusTarefa.A_FAZER), EMAIL))
                .isInstanceOf(PermissaoNegadaException.class);
    }

    @Test
    void membroComCriarSubtarefasCriaSubtarefa() {
        comPermissoes("CRIARSUBTAREFAS");
        tarefa(100L, StatusTarefa.A_FAZER, null);

        TarefaResponseDTO dto = tarefaService.criarTarefa(PROJETO_ID, criarDTO(100L, StatusTarefa.A_FAZER), EMAIL);

        assertThat(dto.tarefaParenteId()).isEqualTo(100L);
    }

    @Test
    void quemTemCriarTarefasTambemCriaSubtarefa() {
        comPermissoes("CRIARTAREFAS");
        tarefa(100L, StatusTarefa.A_FAZER, null);

        TarefaResponseDTO dto = tarefaService.criarTarefa(PROJETO_ID, criarDTO(100L, StatusTarefa.A_FAZER), EMAIL);

        assertThat(dto.tarefaParenteId()).isEqualTo(100L);
    }

    @Test
    void semNenhumaPermissaoDeCriacaoNaoCriaSubtarefa() {
        comPermissoes("EDITARTAREFAS");
        tarefa(100L, StatusTarefa.A_FAZER, null);

        assertThatThrownBy(() -> tarefaService.criarTarefa(PROJETO_ID, criarDTO(100L, StatusTarefa.A_FAZER), EMAIL))
                .isInstanceOf(PermissaoNegadaException.class);
    }

    // ---- Hierarquia ----

    @Test
    void naoCriaSubtarefaDeSubtarefa() {
        tarefa(100L, StatusTarefa.A_FAZER, null);
        tarefa(101L, StatusTarefa.A_FAZER, 100L);

        assertThatThrownBy(() -> tarefaService.criarTarefa(PROJETO_ID, criarDTO(101L, StatusTarefa.A_FAZER), EMAIL))
                .isInstanceOf(HierarquiaTarefaInvalidaException.class);
    }

    @Test
    void naoCriaSubtarefaPendenteEmPaiConcluido() {
        tarefa(100L, StatusTarefa.CONCLUIDA, null);

        assertThatThrownBy(() -> tarefaService.criarTarefa(PROJETO_ID, criarDTO(100L, StatusTarefa.A_FAZER), EMAIL))
                .isInstanceOf(TarefaPaiConcluidaException.class);
    }

    @Test
    void tarefaNaoPodeSerPaiDeSiMesma() {
        tarefa(100L, StatusTarefa.A_FAZER, null);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 100L, editarDTO(100L), EMAIL))
                .isInstanceOf(HierarquiaTarefaInvalidaException.class);
    }

    @Test
    void tarefaComSubtarefasNaoPodeVirarSubtarefa() {
        tarefa(100L, StatusTarefa.A_FAZER, null);
        tarefa(200L, StatusTarefa.A_FAZER, null);
        when(tarefaRepository.existsByTarefaParenteId(200L)).thenReturn(true);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 200L, editarDTO(100L), EMAIL))
                .isInstanceOf(HierarquiaTarefaInvalidaException.class);
    }

    @Test
    void desvincularPaiComMenosUm() {
        Tarefa subtarefa = tarefa(101L, StatusTarefa.A_FAZER, 100L);

        tarefaService.editarTarefa(PROJETO_ID, 101L, editarDTO(-1L), EMAIL);

        assertThat(subtarefa.getTarefaParenteId()).isNull();
    }

    // ---- Transições de status ----

    @Test
    void paiNaoConcluiComSubtarefasPendentes() {
        tarefa(100L, StatusTarefa.FAZENDO, null);
        when(tarefaRepository.existsByTarefaParenteIdAndStatusNot(100L, StatusTarefa.CONCLUIDA)).thenReturn(true);

        assertThatThrownBy(() -> tarefaService.atualizarStatus(PROJETO_ID, 100L, statusDTO(StatusTarefa.CONCLUIDA), EMAIL))
                .isInstanceOf(SubtarefasPendentesException.class);
    }

    @Test
    void paiConcluiQuandoTodasSubtarefasConcluidas() {
        Tarefa pai = tarefa(100L, StatusTarefa.FAZENDO, null);

        tarefaService.atualizarStatus(PROJETO_ID, 100L, statusDTO(StatusTarefa.CONCLUIDA), EMAIL);

        assertThat(pai.getStatus()).isEqualTo(StatusTarefa.CONCLUIDA);
    }

    @Test
    void naoReabreSubtarefaComPaiConcluido() {
        tarefa(100L, StatusTarefa.CONCLUIDA, null);
        tarefa(101L, StatusTarefa.CONCLUIDA, 100L);

        assertThatThrownBy(() -> tarefaService.atualizarStatus(PROJETO_ID, 101L, statusDTO(StatusTarefa.FAZENDO), EMAIL))
                .isInstanceOf(TarefaPaiConcluidaException.class);
    }

    @Test
    void reabreSubtarefaComPaiAberto() {
        tarefa(100L, StatusTarefa.FAZENDO, null);
        Tarefa subtarefa = tarefa(101L, StatusTarefa.CONCLUIDA, 100L);

        tarefaService.atualizarStatus(PROJETO_ID, 101L, statusDTO(StatusTarefa.FAZENDO), EMAIL);

        assertThat(subtarefa.getStatus()).isEqualTo(StatusTarefa.FAZENDO);
    }

    @Test
    void reabreSubtarefaOrfaDeDadoLegado() {
        Tarefa subtarefa = tarefa(101L, StatusTarefa.CONCLUIDA, 999L);

        tarefaService.atualizarStatus(PROJETO_ID, 101L, statusDTO(StatusTarefa.A_FAZER), EMAIL);

        assertThat(subtarefa.getStatus()).isEqualTo(StatusTarefa.A_FAZER);
    }

    // ---- Exclusão ----

    @Test
    void naoExcluiTarefaComSubtarefas() {
        tarefa(100L, StatusTarefa.A_FAZER, null);
        when(tarefaRepository.existsByTarefaParenteId(100L)).thenReturn(true);

        assertThatThrownBy(() -> tarefaService.deletarTarefa(PROJETO_ID, 100L, EMAIL))
                .isInstanceOf(TarefaPossuiSubtarefasException.class);
        verify(tarefaRepository, never()).delete(any());
    }

    @Test
    void excluiTarefaSemSubtarefas() {
        Tarefa tarefa = tarefa(100L, StatusTarefa.A_FAZER, null);

        tarefaService.deletarTarefa(PROJETO_ID, 100L, EMAIL);

        verify(tarefaRepository).delete(tarefa);
    }

    // ---- Listagem ----

    @Test
    void listaSubtarefasComContagemDeConcluidas() {
        tarefa(100L, StatusTarefa.FAZENDO, null);
        Tarefa sub1 = tarefa(101L, StatusTarefa.CONCLUIDA, 100L);
        Tarefa sub2 = tarefa(102L, StatusTarefa.A_FAZER, 100L);
        when(tarefaRepository.findByTarefaParenteId(100L)).thenReturn(List.of(sub1, sub2));

        List<TarefaResponseDTO> subtarefas = tarefaService.listarSubtarefas(PROJETO_ID, 100L);

        assertThat(subtarefas).extracting(TarefaResponseDTO::id).containsExactly(101L, 102L);
    }

    // ---- Dependências: marcar e remover ----

    @Test
    void marcaTarefaComoBloqueadaPorOutra() {
        tarefa(200L, StatusTarefa.A_FAZER, null);
        tarefa(201L, StatusTarefa.A_FAZER, null);

        TarefaResponseDTO dto = tarefaService.editarTarefa(PROJETO_ID, 201L, bloqueioDTO(200L), EMAIL);

        assertThat(dto.bloqueadaPorId()).isEqualTo(200L);
        assertThat(dto.bloqueada()).isTrue();
    }

    @Test
    void naoApareceBloqueadaQuandoPredecessoraConcluida() {
        tarefa(200L, StatusTarefa.CONCLUIDA, null);
        tarefa(201L, StatusTarefa.A_FAZER, null);

        TarefaResponseDTO dto = tarefaService.editarTarefa(PROJETO_ID, 201L, bloqueioDTO(200L), EMAIL);

        assertThat(dto.bloqueadaPorId()).isEqualTo(200L);
        assertThat(dto.bloqueada()).isFalse();
    }

    @Test
    void removeBloqueioComMenosUm() {
        tarefa(200L, StatusTarefa.A_FAZER, null);
        Tarefa bloqueada = bloqueadaPor(tarefa(201L, StatusTarefa.A_FAZER, null), 200L);

        tarefaService.editarTarefa(PROJETO_ID, 201L, bloqueioDTO(-1L), EMAIL);

        assertThat(bloqueada.getBloqueadaPorId()).isNull();
    }

    @Test
    void subtarefaPodeSerBloqueadaPorIrma() {
        tarefa(100L, StatusTarefa.A_FAZER, null);
        tarefa(101L, StatusTarefa.A_FAZER, 100L);
        Tarefa irma = tarefa(102L, StatusTarefa.A_FAZER, 100L);

        tarefaService.editarTarefa(PROJETO_ID, 102L, bloqueioDTO(101L), EMAIL);

        assertThat(irma.getBloqueadaPorId()).isEqualTo(101L);
    }

    // ---- Dependências: validações ----

    @Test
    void tarefaNaoPodeSerBloqueadaPorSiMesma() {
        tarefa(200L, StatusTarefa.A_FAZER, null);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 200L, bloqueioDTO(200L), EMAIL))
                .isInstanceOf(DependenciaInvalidaException.class);
    }

    @Test
    void subtarefaNaoPodeSerBloqueadaPeloPai() {
        tarefa(100L, StatusTarefa.A_FAZER, null);
        tarefa(101L, StatusTarefa.A_FAZER, 100L);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 101L, bloqueioDTO(100L), EMAIL))
                .isInstanceOf(DependenciaInvalidaException.class);
    }

    @Test
    void paiNaoPodeSerBloqueadoPorSubtarefa() {
        tarefa(100L, StatusTarefa.A_FAZER, null);
        tarefa(101L, StatusTarefa.A_FAZER, 100L);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 100L, bloqueioDTO(101L), EMAIL))
                .isInstanceOf(DependenciaInvalidaException.class);
    }

    @Test
    void naoViraSubtarefaDeQuemEstaBloqueando() {
        bloqueadaPor(tarefa(100L, StatusTarefa.A_FAZER, null), 200L);
        tarefa(200L, StatusTarefa.A_FAZER, null);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 200L, editarDTO(100L), EMAIL))
                .isInstanceOf(DependenciaInvalidaException.class);
    }

    @Test
    void naoCriaCicloDireto() {
        bloqueadaPor(tarefa(200L, StatusTarefa.A_FAZER, null), 201L);
        tarefa(201L, StatusTarefa.A_FAZER, null);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 201L, bloqueioDTO(200L), EMAIL))
                .isInstanceOf(DependenciaCircularException.class);
    }

    @Test
    void naoCriaCicloIndireto() {
        // A bloqueada por B, B bloqueada por C; tentar C bloqueada por A
        bloqueadaPor(tarefa(300L, StatusTarefa.A_FAZER, null), 301L);
        bloqueadaPor(tarefa(301L, StatusTarefa.A_FAZER, null), 302L);
        tarefa(302L, StatusTarefa.A_FAZER, null);

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 302L, bloqueioDTO(300L), EMAIL))
                .isInstanceOf(DependenciaCircularException.class);
    }

    @Test
    void naoCriaCicloPassandoPelaHierarquia() {
        // S é subtarefa de P; X é bloqueada por P; tentar S bloqueada por X
        tarefa(100L, StatusTarefa.A_FAZER, null);
        Tarefa sub = tarefa(101L, StatusTarefa.A_FAZER, 100L);
        bloqueadaPor(tarefa(400L, StatusTarefa.A_FAZER, null), 100L);
        when(tarefaRepository.findByTarefaParenteId(100L)).thenReturn(List.of(sub));

        assertThatThrownBy(() -> tarefaService.editarTarefa(PROJETO_ID, 101L, bloqueioDTO(400L), EMAIL))
                .isInstanceOf(DependenciaCircularException.class);
    }

    @Test
    void naoCriaTarefaJaEmFazendoComPredecessoraPendente() {
        tarefa(200L, StatusTarefa.A_FAZER, null);

        assertThatThrownBy(() -> tarefaService.criarTarefa(PROJETO_ID, criarDTO(null, StatusTarefa.FAZENDO, 200L), EMAIL))
                .isInstanceOf(TarefaBloqueadaException.class);
    }

    @Test
    void criaTarefaBloqueadaEmAFazer() {
        tarefa(200L, StatusTarefa.A_FAZER, null);

        TarefaResponseDTO dto = tarefaService.criarTarefa(PROJETO_ID, criarDTO(null, StatusTarefa.A_FAZER, 200L), EMAIL);

        assertThat(dto.bloqueadaPorId()).isEqualTo(200L);
        assertThat(dto.bloqueada()).isTrue();
    }

    // ---- Dependências: regra de status ----

    @Test
    void bloqueadaNaoVaiParaFazendo() {
        tarefa(200L, StatusTarefa.FAZENDO, null);
        bloqueadaPor(tarefa(201L, StatusTarefa.A_FAZER, null), 200L);

        assertThatThrownBy(() -> tarefaService.atualizarStatus(PROJETO_ID, 201L, statusDTO(StatusTarefa.FAZENDO), EMAIL))
                .isInstanceOf(TarefaBloqueadaException.class)
                .hasMessageContaining("Tarefa 200");
    }

    @Test
    void vaiParaFazendoQuandoPredecessoraConcluida() {
        tarefa(200L, StatusTarefa.CONCLUIDA, null);
        Tarefa bloqueada = bloqueadaPor(tarefa(201L, StatusTarefa.A_FAZER, null), 200L);

        tarefaService.atualizarStatus(PROJETO_ID, 201L, statusDTO(StatusTarefa.FAZENDO), EMAIL);

        assertThat(bloqueada.getStatus()).isEqualTo(StatusTarefa.FAZENDO);
    }

    @Test
    void bloqueioSoImpedeFazendo() {
        // Decisão da equipe: seguir o texto da US024, que só impede "Em Andamento"
        tarefa(200L, StatusTarefa.A_FAZER, null);
        Tarefa bloqueada = bloqueadaPor(tarefa(201L, StatusTarefa.A_FAZER, null), 200L);

        tarefaService.atualizarStatus(PROJETO_ID, 201L, statusDTO(StatusTarefa.TESTE), EMAIL);

        assertThat(bloqueada.getStatus()).isEqualTo(StatusTarefa.TESTE);
    }

    @Test
    void excluirPredecessoraLiberaAsBloqueadas() {
        tarefa(200L, StatusTarefa.A_FAZER, null);
        Tarefa bloqueada = bloqueadaPor(tarefa(201L, StatusTarefa.A_FAZER, null), 200L);
        when(tarefaRepository.findByBloqueadaPorId(200L)).thenReturn(List.of(bloqueada));

        tarefaService.deletarTarefa(PROJETO_ID, 200L, EMAIL);

        assertThat(bloqueada.getBloqueadaPorId()).isNull();
    }

    // ---- Auxiliares ----

    private void comPermissoes(String... descricoes) {
        ProjetoUsuario membro = new ProjetoUsuario();
        membro.setPermissoes(Arrays.stream(descricoes).map(Permissao::new).toList());
        when(projetoUsuarioRepository.findByUsuarioIdAndProjetoId(USUARIO_ID, PROJETO_ID))
                .thenReturn(Optional.of(membro));
    }

    private Tarefa tarefa(Long id, StatusTarefa status, Long tarefaParenteId) {
        Tarefa tarefa = new Tarefa(PROJETO_ID, null, "Tarefa " + id, "desc", null, status, null, tarefaParenteId, false);
        ReflectionTestUtils.setField(tarefa, "id", id);
        when(tarefaRepository.findById(id)).thenReturn(Optional.of(tarefa));
        return tarefa;
    }

    private Tarefa bloqueadaPor(Tarefa tarefa, Long bloqueadaPorId) {
        tarefa.setBloqueadaPorId(bloqueadaPorId);
        return tarefa;
    }

    private CriarTarefaRequestDTO criarDTO(Long tarefaParenteId, StatusTarefa status) {
        return criarDTO(tarefaParenteId, status, null);
    }

    private CriarTarefaRequestDTO criarDTO(Long tarefaParenteId, StatusTarefa status, Long bloqueadaPorId) {
        return new CriarTarefaRequestDTO("Nova", "desc", null, status, null, null, tarefaParenteId, false, null, null, bloqueadaPorId);
    }

    private EditarTarefaRequestDTO editarDTO(Long tarefaParenteId) {
        return new EditarTarefaRequestDTO(null, null, null, null, null, tarefaParenteId, null, null);
    }

    private EditarTarefaRequestDTO bloqueioDTO(Long bloqueadaPorId) {
        return new EditarTarefaRequestDTO(null, null, null, null, null, null, null, bloqueadaPorId);
    }

    private AtualizarStatusTarefaRequestDTO statusDTO(StatusTarefa status) {
        return new AtualizarStatusTarefaRequestDTO(status);
    }
}
