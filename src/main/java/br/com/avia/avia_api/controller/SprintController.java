package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.sprint.*;
import br.com.avia.avia_api.dto.tarefa.*;
import br.com.avia.avia_api.service.SprintService;
import br.com.avia.avia_api.service.TarefaService;
import br.com.avia.avia_api.service.VotoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/projetos")
@Tag(name = "Sprint", description = "Gerenciamento de sprints")
public class SprintController {

    private final SprintService sprintService;
    private final TarefaService tarefaService;
    private final VotoService votoService;

    public SprintController(SprintService sprintService, TarefaService tarefaService, VotoService votoService) {
        this.sprintService = sprintService;
        this.tarefaService = tarefaService;
        this.votoService = votoService;
    }

    /* ── Sprints ── */

    @PostMapping("/{projeto_id}/sprints")
    public ResponseEntity<SprintResponseDTO> criarSprint(
            @PathVariable("projeto_id") Long projetoId,
            @RequestBody @Valid CriarSprintRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        SprintResponseDTO sprint = sprintService.criarSprint(projetoId, dto, emailUsuario);
        return new ResponseEntity<>(sprint, HttpStatus.CREATED);
    }

    @GetMapping("/{projeto_id}/sprints")
    public ResponseEntity<List<SprintResponseDTO>> listarSprints(
            @PathVariable("projeto_id") Long projetoId,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        List<SprintResponseDTO> sprints = sprintService.listarSprintsPorProjeto(projetoId, emailUsuario);
        return ResponseEntity.ok(sprints);
    }

    @PatchMapping("/{projeto_id}/sprints/{sprint_id}")
    public ResponseEntity<SprintResponseDTO> editarSprint(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @RequestBody EditarSprintRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        SprintResponseDTO sprint = sprintService.editarSprint(projetoId, sprintId, dto, emailUsuario);
        return ResponseEntity.ok(sprint);
    }

    /* ── Backlog ── */

    @GetMapping("/{projeto_id}/backlog")
    public ResponseEntity<BacklogResponseDTO> getBacklog(
            @PathVariable("projeto_id") Long projetoId,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        BacklogResponseDTO backlog = sprintService.getOuCriarBacklog(projetoId, emailUsuario);
        return ResponseEntity.ok(backlog);
    }

    /* ── Tarefas ── */

    @GetMapping("/{projeto_id}/sprints/{sprint_id}/tarefas")
    public ResponseEntity<List<TarefaResponseDTO>> listarTarefas(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @AuthenticationPrincipal Jwt jwt) {
        List<TarefaResponseDTO> tarefas = tarefaService.listarTarefas(projetoId, sprintId);
        return ResponseEntity.ok(tarefas);
    }

    @PostMapping("/{projeto_id}/sprints/{sprint_id}/tarefas")
    public ResponseEntity<TarefaResponseDTO> criarTarefa(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @RequestBody @Valid CriarTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        TarefaResponseDTO tarefa = tarefaService.criarTarefa(projetoId, sprintId, dto);
        return new ResponseEntity<>(tarefa, HttpStatus.CREATED);
    }

    @PutMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}")
    public ResponseEntity<TarefaResponseDTO> atualizarTarefa(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @RequestBody AtualizarTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        TarefaResponseDTO tarefa = tarefaService.atualizarTarefa(projetoId, sprintId, tarefaId, dto);
        return ResponseEntity.ok(tarefa);
    }

    @PatchMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}")
    public ResponseEntity<TarefaResponseDTO> moverTarefa(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @RequestBody @Valid MoverTarefaRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        TarefaResponseDTO tarefa = tarefaService.moverTarefaParaSprint(projetoId, sprintId, tarefaId, dto.novaSprintId());
        return ResponseEntity.ok(tarefa);
    }

    @DeleteMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}")
    public ResponseEntity<Void> removerTarefaDaSprint(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @AuthenticationPrincipal Jwt jwt) {
        tarefaService.removerTarefaDaSprint(projetoId, sprintId, tarefaId);
        return ResponseEntity.noContent().build();
    }

    /* ── Planning Poker (RF015) ── */

    @GetMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}/votos")
    public ResponseEntity<SessaoPokerResponseDTO> getSessaoPoker(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        SessaoPokerResponseDTO sessao = votoService.getSessao(projetoId, sprintId, tarefaId, emailUsuario);
        return ResponseEntity.ok(sessao);
    }

    @PostMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}/votos")
    public ResponseEntity<VotoResponseDTO> votar(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @RequestBody @Valid VotoRequestDTO dto,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        VotoResponseDTO voto = votoService.votar(projetoId, sprintId, tarefaId, dto.valor(), emailUsuario);
        return new ResponseEntity<>(voto, HttpStatus.CREATED);
    }

    @PostMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}/votos/consolidar")
    public ResponseEntity<TarefaResponseDTO> consolidar(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        TarefaResponseDTO tarefa = votoService.consolidar(projetoId, sprintId, tarefaId, emailUsuario);
        return ResponseEntity.ok(tarefa);
    }

    @DeleteMapping("/{projeto_id}/sprints/{sprint_id}/tarefas/{tarefa_id}/votos")
    public ResponseEntity<Map<String, String>> resetarVotos(
            @PathVariable("projeto_id") Long projetoId,
            @PathVariable("sprint_id") Long sprintId,
            @PathVariable("tarefa_id") Long tarefaId,
            @AuthenticationPrincipal Jwt jwt) {
        String emailUsuario = jwt.getSubject();
        votoService.resetar(projetoId, sprintId, tarefaId, emailUsuario);
        return ResponseEntity.ok(Map.of("mensagem", "Votos resetados com sucesso."));
    }
}
