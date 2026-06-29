
package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.tarefa.*;
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
        TarefaResponseDTO tarefa = tarefaService.criarTarefa(projetoId, dto);
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
        TarefaResponseDTO tarefa =
                tarefaService.atualizarStatus(projetoId, tarefaId, dto);
        return ResponseEntity.ok(tarefa);
    }

    @PatchMapping("/{projetoId}/tarefas/{tarefaId}")
    public ResponseEntity<TarefaResponseDTO> editarTarefa(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody @Valid EditarTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa =
                tarefaService.editarTarefa(projetoId, tarefaId, dto);

        return ResponseEntity.ok(tarefa);
    }

    @PatchMapping("/{projetoId}/tarefas/{tarefaId}/sprint")
    public ResponseEntity<TarefaResponseDTO> atualizarSprint(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @RequestBody AtualizarSprintTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa =
                tarefaService.atualizarSprint(projetoId, tarefaId, dto);

        return ResponseEntity.ok(tarefa);
    }

    @DeleteMapping("/{projetoId}/tarefas/{tarefaId}")
    public ResponseEntity<Void> deletarTarefa(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        tarefaService.deletarTarefa(projetoId, tarefaId);
        return ResponseEntity.noContent().build();
    }


}
