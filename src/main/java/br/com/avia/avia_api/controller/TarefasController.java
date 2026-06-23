
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
}
