
package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.tarefa.*;
import br.com.avia.avia_api.enums.StatusTarefa;
import br.com.avia.avia_api.service.TarefaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/projetos")
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas")
public class TarefasController {

    private final TarefaService tarefaService;

    public TarefasController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping("/{projetoId}/tarefas")
    public ResponseEntity<TarefaResponseDTO> criarTarefa(
            @PathVariable Long projetoId,
            @RequestBody @Valid CriarTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailMembro = jwt.getSubject();
        TarefaResponseDTO tarefa = tarefaService.criarTarefa(projetoId, dto, emailMembro);
        return new ResponseEntity<>(tarefa, HttpStatus.CREATED);
    }

    @GetMapping("/{projetoId}/tarefas/backlog")
    public ResponseEntity<List<TarefaResponseDTO>> listarBacklog(
            @PathVariable Long projetoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prioridade,
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<TarefaResponseDTO> tarefas =
                tarefaService.listarBacklog(projetoId, status, prioridade);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/{projetoId}/tarefas/historico")
    public ResponseEntity<List<TarefaResponseDTO>> filtrarHistorico(
            @PathVariable Long projetoId,
            @RequestParam(required = false) List<Long> responsaveisIds,
            @RequestParam(required = false) Long sprintInicioId,
            @RequestParam(required = false) Long sprintFimId,
            @RequestParam(required = false) List<StatusTarefa> status,
            @RequestParam(required = false) Boolean impedimento,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailMembro = jwt.getSubject();
        FiltroHistoricoDTO filtro =
                new FiltroHistoricoDTO(responsaveisIds, sprintInicioId, sprintFimId, status, impedimento);
        List<TarefaResponseDTO> tarefas =
                tarefaService.filtrarHistorico(projetoId, filtro, emailMembro);
        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/{projetoId}/tarefas/{tarefaId}")
    public ResponseEntity<TarefaResponseDTO> buscarTarefaPorId(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa =
                tarefaService.buscarTarefaPorId(projetoId, tarefaId);
        return ResponseEntity.ok(tarefa);
    }

    @GetMapping("/{projetoId}/tarefas/{tarefaId}/subtarefas")
    public ResponseEntity<List<TarefaResponseDTO>> listarSubtarefas(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailMembro = jwt.getSubject();
        List<TarefaResponseDTO> subtarefas =
                tarefaService.listarSubtarefas(projetoId, tarefaId, emailMembro);
        return ResponseEntity.ok(subtarefas);
    }

    @GetMapping("/{projetoId}/tarefas")
    public ResponseEntity<List<TarefaResponseDTO>> listarTarefasDoProjeto(
            @PathVariable Long projetoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prioridade,
            @RequestParam(required = false) Long sprintId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<TarefaResponseDTO> tarefas =
                tarefaService.listarTarefasDoProjeto(projetoId, status, prioridade, sprintId);

        return ResponseEntity.ok(tarefas);
    }

    @PatchMapping("/{projetoId}/tarefas/{tarefaId}/status")
    public ResponseEntity<TarefaResponseDTO> atualizarStatus(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody @Valid AtualizarStatusTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        TarefaResponseDTO tarefa = tarefaService.atualizarStatus(projetoId, tarefaId, dto, email);
        return ResponseEntity.ok(tarefa);
    }

    @PatchMapping("/{projetoId}/tarefas/{tarefaId}")
    public ResponseEntity<TarefaResponseDTO> editarTarefa(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody @Valid EditarTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailMembro = jwt.getSubject();
        TarefaResponseDTO tarefa =
                tarefaService.editarTarefa(projetoId, tarefaId, dto, emailMembro);

        return ResponseEntity.ok(tarefa);
    }

    @PatchMapping("/{projetoId}/tarefas/{tarefaId}/sprint")
    public ResponseEntity<TarefaResponseDTO> atualizarSprint(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody AtualizarSprintTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailMembro = jwt.getSubject();
        TarefaResponseDTO tarefa =
                tarefaService.atualizarSprint(projetoId, tarefaId, dto, emailMembro);

        return ResponseEntity.ok(tarefa);
    }

    @DeleteMapping("/{projetoId}/tarefas/{tarefaId}")
    public ResponseEntity<Void> deletarTarefa(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailMembro = jwt.getSubject();

        tarefaService.deletarTarefa(projetoId, tarefaId, emailMembro);
        return ResponseEntity.noContent().build();
    }


}
