package br.com.avia.avia_api.service;

import br.com.avia.avia_api.dto.tarefa.CriarTarefaRequestDTO;
import br.com.avia.avia_api.dto.tarefa.ResponsavelDTO;
import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;
import br.com.avia.avia_api.entity.Permissao;
import br.com.avia.avia_api.entity.Projeto;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Sprint;
import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.entity.TarefaProjetoUsuario;
import br.com.avia.avia_api.entity.Usuario;
import br.com.avia.avia_api.enums.PrioridadeTarefa;
import br.com.avia.avia_api.enums.StatusSprint;
import br.com.avia.avia_api.enums.StatusTarefa;
import br.com.avia.avia_api.repository.AnexoRepository;
import br.com.avia.avia_api.repository.ProjetoRepository;
import br.com.avia.avia_api.repository.ProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.SprintRepository;
import br.com.avia.avia_api.repository.TarefaProjetoUsuarioRepository;
import br.com.avia.avia_api.repository.TarefaRepository;
import br.com.avia.avia_api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Testa TarefaService.criarTarefa olhando para a SAIDA (TarefaResponseDTO)
 * em funcao da ENTRADA (CriarTarefaRequestDTO).
 *
 * Premissas (mudancas da numeracao por projeto que voce ja deve ter feito):
 *  - TarefaService recebe ProjetoRepository e usa buscarComLock(projetoId) em criarTarefa;
 *  - Projeto tem o campo proximoNumeroTarefa e consumirProximoNumeroTarefa();
 *  - TarefaResponseDTO tem o campo Integer numero, alem do id.
 */
@ExtendWith(MockitoExtension.class)
class CriarTarefaTest {

    private static final Long PROJETO_ID = 1L;
    private static final Long USUARIO_ID = 10L;
    private static final String EMAIL = "membro@avia.com";

    // o banco gera o id no INSERT; no teste simulamos isso no save()
    private static final Long ID_GERADO_PELO_BANCO = 120L;

    private static final Long SPRINT_ID = 30L;
    private static final Long ANA_ID = 20L;
    private static final Long BRUNO_ID = 21L;

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock private TarefaRepository tarefaRepository;
    @Mock private SprintRepository sprintRepository;
    @Mock private TarefaProjetoUsuarioRepository tarefaProjetoUsuarioRepository;
    @Mock private AnexoRepository anexoRepository;
    @Mock private ProjetoRepository projetoRepository;

    @InjectMocks
    private TarefaService tarefaService;

    private Projeto projeto;

    // simula a tabela tarefa_projeto_usuarios: o que for salvo aparece na leitura
    private final List<TarefaProjetoUsuario> vinculosSalvos = new ArrayList<>();

// quando houver alguma dependencoia extre na tarefa, atualizar o teste
    @Test
    void criaTarefaSoComCamposObrigatoriosEDevolveDtoCoerente() {
        membroComPermissaoDeCriar();
        projetoComProximoNumero(1);
        bancoGeraIdAoSalvarTarefa();

        TarefaResponseDTO saida = tarefaService.criarTarefa(PROJETO_ID, entradaMinima(), EMAIL);

        assertSoftly(softly -> {
            softly.assertThat(saida.id()).isEqualTo(ID_GERADO_PELO_BANCO);
            softly.assertThat(saida.numeroTarefa()).isEqualTo(1);
            softly.assertThat(saida.titulo()).isEqualTo("Criar tela de login");
            softly.assertThat(saida.descricao()).isEqualTo("Tela com email e senha");
            softly.assertThat(saida.status()).isEqualTo(StatusTarefa.A_FAZER);

            // opcionais nao informados continuam vazios
            softly.assertThat(saida.estimativa()).isNull();
            softly.assertThat(saida.prioridade()).isNull();
            softly.assertThat(saida.sprintId()).isNull();
            softly.assertThat(saida.tarefaParenteId()).isNull();
            softly.assertThat(saida.bloqueadaPorId()).isNull();
            softly.assertThat(saida.tipo()).isNull();

            // impedimento nao informado vira false (Boolean.TRUE.equals(null))
            softly.assertThat(saida.impedimento()).isFalse();
            softly.assertThat(saida.bloqueada()).isFalse();

            // campos calculados de uma tarefa recem criada
            softly.assertThat(saida.responsaveis()).isEmpty();
            softly.assertThat(saida.subtarefasCount()).isZero();
            softly.assertThat(saida.subtarefasConcluidasCount()).isZero();
            softly.assertThat(saida.anexosCount()).isZero();
            softly.assertThat(saida.atividadeFutura()).isFalse();
            softly.assertThat(saida.sprintNome()).isNull();
        });
    }

    @Test
    void criaTarefaCompletaEDevolveNoDtoTudoQueFoiInformado() {
        membroComPermissaoDeCriar();
        projetoComProximoNumero(1);
        bancoGeraIdAoSalvarTarefa();
        sprintEmPlanejamento(SPRINT_ID, "Sprint 1");
        responsavel(ANA_ID, "Ana", 200L);
        responsavel(BRUNO_ID, "Bruno", 201L);
        bancoGuardaVinculosDeResponsaveis();

        CriarTarefaRequestDTO entrada = new CriarTarefaRequestDTO(
                "Implementar login com JWT",   // titulo
                "Endpoint de autenticacao",    // descricao
                8,                             // estimativa
                StatusTarefa.FAZENDO,          // status
                PrioridadeTarefa.ALTA,         // prioridade
                SPRINT_ID,                     // sprintId
                null,                          // tarefaParenteId
                true,                          // impedimento
                List.of(ANA_ID, BRUNO_ID),     // responsaveisIds
                "BACKEND",                     // tipo
                null                           // bloqueadaPorId
        );

        TarefaResponseDTO saida = tarefaService.criarTarefa(PROJETO_ID, entrada, EMAIL);

        assertSoftly(softly -> {
            softly.assertThat(saida.id()).isEqualTo(ID_GERADO_PELO_BANCO);
            softly.assertThat(saida.numeroTarefa()).isEqualTo(1);
            softly.assertThat(saida.titulo()).isEqualTo("Implementar login com JWT");
            softly.assertThat(saida.descricao()).isEqualTo("Endpoint de autenticacao");
            softly.assertThat(saida.estimativa()).isEqualTo(8);
            softly.assertThat(saida.status()).isEqualTo(StatusTarefa.FAZENDO);
            softly.assertThat(saida.prioridade()).isEqualTo(PrioridadeTarefa.ALTA);
            softly.assertThat(saida.sprintId()).isEqualTo(SPRINT_ID);
            softly.assertThat(saida.impedimento()).isTrue();
            softly.assertThat(saida.tipo()).isEqualTo("BACKEND");

            // responsaveis: mesmos usuarios da entrada, na mesma ordem
            softly.assertThat(saida.responsaveis()).containsExactly(
                    new ResponsavelDTO(ANA_ID, "Ana"),
                    new ResponsavelDTO(BRUNO_ID, "Bruno"));

            // sprint em planejamento => atividade futura, com o nome da sprint
            softly.assertThat(saida.atividadeFutura()).isTrue();
            softly.assertThat(saida.sprintNome()).isEqualTo("Sprint 1");

            softly.assertThat(saida.tarefaParenteId()).isNull();
            softly.assertThat(saida.bloqueadaPorId()).isNull();
            softly.assertThat(saida.bloqueada()).isFalse();
            softly.assertThat(saida.subtarefasCount()).isZero();
            softly.assertThat(saida.anexosCount()).isZero();
        });
    }

    @Test
    void numeroDeTarefaExcluidaNaoEhReaproveitadoENovaTarefaRecebeOProximo() {
        membroComPermissoes("CRIARTAREFAS", "EXCLUIRTAREFAS");
        projetoComProximoNumero(1);
        bancoGeraIdAoSalvarTarefa();

        // 1. cria a primeira tarefa: recebe o numero 1
        TarefaResponseDTO primeira = tarefaService.criarTarefa(PROJETO_ID, entradaMinima(), EMAIL);

        // pega a entidade que o service mandou salvar; ela faz o papel da linha que agora existe no banco
        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        verify(tarefaRepository).save(captor.capture());
        Tarefa tarefaCriada = captor.getValue();
        when(tarefaRepository.findById(primeira.id())).thenReturn(Optional.of(tarefaCriada));

        // 2. exclui essa tarefa
        tarefaService.deletarTarefa(PROJETO_ID, primeira.id(), EMAIL);
        verify(tarefaRepository).delete(tarefaCriada);

        // 3. cria outra tarefa no mesmo projeto
        TarefaResponseDTO segunda = tarefaService.criarTarefa(PROJETO_ID, entradaMinima(), EMAIL);

        // a excluida tinha o 1; a nova NAO reaproveita: recebe o 2 (excluida++)
        assertThat(primeira.numeroTarefa()).isEqualTo(1);
        assertThat(segunda.numeroTarefa()).isEqualTo(2);
        assertThat(projeto.getProximoNumeroTarefa()).isEqualTo(3);
    }

    @Test
    void numeroVemDoContadorDoProjetoEOIdContinuaSendoOGlobal() {
        // projeto que ja tem 3 tarefas: a proxima deve ser a 4
        membroComPermissaoDeCriar();
        projetoComProximoNumero(4);
        bancoGeraIdAoSalvarTarefa();

        TarefaResponseDTO saida = tarefaService.criarTarefa(PROJETO_ID, entradaMinima(), EMAIL);

        assertThat(saida.numeroTarefa()).isEqualTo(4);
        assertThat(saida.id()).isEqualTo(ID_GERADO_PELO_BANCO);
        assertThat(projeto.getProximoNumeroTarefa()).isEqualTo(5);
    }

    // ---- Entradas ----

    private CriarTarefaRequestDTO entradaMinima() {
        return new CriarTarefaRequestDTO(
                "Criar tela de login",         // titulo
                "Tela com email e senha",      // descricao
                null,                          // estimativa
                StatusTarefa.A_FAZER,          // status
                null,                          // prioridade
                null,                          // sprintId
                null,                          // tarefaParenteId
                null,                          // impedimento
                null,                          // responsaveisIds
                null,                          // tipo
                null                           // bloqueadaPorId
        );
    }

    // ---- Cenario (o que os mocks respondem) ----

    private void membroComPermissoes(String... descricoes) {
        Usuario usuario = new Usuario("Membro", EMAIL, "senha");
        ReflectionTestUtils.setField(usuario, "id", USUARIO_ID);
        when(usuarioRepository.findByEmail(EMAIL)).thenReturn(Optional.of(usuario));

        ProjetoUsuario vinculo = new ProjetoUsuario();
        vinculo.setPermissoes(Arrays.stream(descricoes).map(Permissao::new).toList());
        when(projetoUsuarioRepository.findByUsuarioIdAndProjetoId(USUARIO_ID, PROJETO_ID))
                .thenReturn(Optional.of(vinculo));
    }

    private void membroComPermissaoDeCriar() {
        membroComPermissoes("CRIARTAREFAS");
    }


    private void projetoComProximoNumero(int proximoNumero) {
        projeto = new Projeto();
        ReflectionTestUtils.setField(projeto, "proximoNumeroTarefa", proximoNumero);
        when(projetoRepository.buscarComLock(PROJETO_ID)).thenReturn(Optional.of(projeto));
    }

    // simula o INSERT: devolve a mesma tarefa, agora com o id que o banco geraria
    private void bancoGeraIdAoSalvarTarefa() {
        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocacao -> {
            Tarefa tarefa = invocacao.getArgument(0);
            ReflectionTestUtils.setField(tarefa, "id", ID_GERADO_PELO_BANCO);
            return tarefa;
        });
    }

    private void sprintEmPlanejamento(Long id, String nome) {
        Sprint sprint = new Sprint(PROJETO_ID, nome, StatusSprint.EM_PLANEJAMENTO);
        ReflectionTestUtils.setField(sprint, "id", id);
        when(sprintRepository.findById(id)).thenReturn(Optional.of(sprint));
    }

    // um membro do projeto que pode ser escolhido como responsavel
    private void responsavel(Long usuarioId, String nome, Long projetoUsuarioId) {
        Usuario usuario = new Usuario(nome, usuarioId + "@avia.com", "senha");
        ReflectionTestUtils.setField(usuario, "id", usuarioId);

        ProjetoUsuario vinculo = new ProjetoUsuario();
        ReflectionTestUtils.setField(vinculo, "id", projetoUsuarioId);
        ReflectionTestUtils.setField(vinculo, "usuario", usuario);

        when(projetoUsuarioRepository.findByUsuarioIdAndProjetoId(usuarioId, PROJETO_ID))
                .thenReturn(Optional.of(vinculo));
        when(projetoUsuarioRepository.findById(projetoUsuarioId)).thenReturn(Optional.of(vinculo));
    }

    // grava vinculos em uma lista e devolve essa mesma lista na leitura, como a tabela faria
    private void bancoGuardaVinculosDeResponsaveis() {
        when(tarefaProjetoUsuarioRepository.save(any(TarefaProjetoUsuario.class))).thenAnswer(invocacao -> {
            TarefaProjetoUsuario vinculo = invocacao.getArgument(0);
            vinculosSalvos.add(vinculo);
            return vinculo;
        });
        when(tarefaProjetoUsuarioRepository.findByTarefaId(ID_GERADO_PELO_BANCO)).thenReturn(vinculosSalvos);
    }
}
