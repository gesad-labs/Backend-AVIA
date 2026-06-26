
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

    @PostMapping("/{projeto_id}/sprints/{sprint_id}/tarefas")
    public ResponseEntity<TarefaResponseDTO> criarTarefa(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @RequestBody @Valid CriarTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa = tarefaService.criarTarefa(projetoId, sprintId, dto);
        return new ResponseEntity<>(tarefa, HttpStatus.CREATED);
    }

    @DeleteMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}")
    public ResponseEntity<Void> removerTarefaDaSprint(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        tarefaService.removerTarefaDaSprint(projetoId, sprintId, tarefaId);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{projeto_id}/tarefas")
    public ResponseEntity<TarefaResponseDTO> criarTarefaBacklog(
            @PathVariable("projeto_id") Long projetoId,
            @RequestBody @Valid CriarTarefaBacklogRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa = tarefaService.criarTarefaBacklog(projetoId, dto);
        return new ResponseEntity<>(tarefa, HttpStatus.CREATED);
    }

    @GetMapping("/{projeto_id}/tarefas")
    public ResponseEntity<List<TarefaResponseDTO>> listarBacklog(
            @PathVariable("projeto_id") Long projetoId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String prioridade,
            @AuthenticationPrincipal Jwt jwt
    ) {
        List<TarefaResponseDTO> tarefas =
                tarefaService.listarBacklog(projetoId, status, prioridade);

        return ResponseEntity.ok(tarefas);
    }

    @GetMapping("/{projeto_id}/tarefas/{tarefa_id}")
    public ResponseEntity<TarefaResponseDTO> buscarTarefaPorId(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa =
                tarefaService.buscarTarefaPorId(projetoId, tarefaId);

        return ResponseEntity.ok(tarefa);
    }

    @PatchMapping("/{projeto_id}/tarefas/{tarefa_id}/status")
    public ResponseEntity<TarefaResponseDTO> atualizarStatus(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @RequestBody AtualizarStatusTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa =
                tarefaService.atualizarStatus(projetoId, tarefaId, dto);

        return ResponseEntity.ok(tarefa);
    }

    @PatchMapping("/{projeto_id}/tarefas/{tarefa_id}")
    public ResponseEntity<TarefaResponseDTO> editarTarefa(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @RequestBody EditarTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        TarefaResponseDTO tarefa =
                tarefaService.editarTarefa(projetoId, tarefaId, dto);

        return ResponseEntity.ok(tarefa);
    }

    @PostMapping("/{projeto_id}/tarefas/{tarefa_id}/anexos")
    public ResponseEntity<AnexoResponseDTO> adicionarAnexo(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @RequestBody @Valid CriarAnexoRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        AnexoResponseDTO response =
                tarefaService.adicionarAnexo(projetoId, tarefaId, dto);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{projeto_id}/tarefas/{tarefa_id}/anexos/{anexo_id}")
    public ResponseEntity<Void> removerAnexo(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("tarefa_id") Long tarefaId,
            @PathVariable("anexo_id") Integer anexoId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        tarefaService.removerAnexo(projetoId, tarefaId, anexoId);
        return ResponseEntity.noContent().build();
    }


}
