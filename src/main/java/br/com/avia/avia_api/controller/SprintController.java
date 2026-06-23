package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.sprint.*;
import br.com.avia.avia_api.dto.tarefa.*;
import br.com.avia.avia_api.service.SprintService;
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
@Tag(name = "Sprint", description = "Gerenciamento de sprints")
public class SprintController {

    private final SprintService sprintService;
    private final TarefaService tarefaService;

    public SprintController(SprintService sprintService, TarefaService tarefaService) {
        this.sprintService = sprintService;
        this.tarefaService = tarefaService;
    }

    @PostMapping("/{projeto_id}/sprints")
    public ResponseEntity<SprintResponseDTO> criarSprint(
            @PathVariable("projeto_id") Long projetoId,
            @RequestBody @Valid CriarSprintRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailUsuario = jwt.getSubject();
        SprintResponseDTO sprint = sprintService.criarSprint(projetoId, dto, emailUsuario);
        return new ResponseEntity<>(sprint, HttpStatus.CREATED);
    }

    @GetMapping("/{projeto_id}/sprints")
    public ResponseEntity<List<SprintResponseDTO>> listarSprints(
            @PathVariable("projeto_id") Long projetoId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailUsuario = jwt.getSubject();
        List<SprintResponseDTO> sprints = sprintService.listarSprintsPorProjeto(projetoId, emailUsuario);
        return ResponseEntity.ok(sprints);
    }

    @PatchMapping("/{projeto_id}/sprints/{sprint_id}")
    public ResponseEntity<SprintResponseDTO> editarSprint(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @RequestBody EditarSprintRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String emailUsuario = jwt.getSubject();
        SprintResponseDTO sprint = sprintService.editarSprint(projetoId, sprintId, dto, emailUsuario);
        return ResponseEntity.ok(sprint);
    }
}