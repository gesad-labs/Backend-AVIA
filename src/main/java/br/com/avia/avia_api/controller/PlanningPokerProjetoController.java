package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.service.PlanningPokerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/projetos/{projetoId}/planning-poker")
@Tag(name = "Planning Poker", description = "Estimativa colaborativa — operações a nível de projeto")
public class PlanningPokerProjetoController {

    private final PlanningPokerService planningPokerService;

    public PlanningPokerProjetoController(PlanningPokerService planningPokerService) {
        this.planningPokerService = planningPokerService;
    }

    @GetMapping("/tarefas-com-sessao-ativa")
    public ResponseEntity<List<Long>> listarTarefasComSessaoAtiva(
            @PathVariable Long projetoId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(
                planningPokerService.listarTarefasComSessaoAtiva(projetoId, jwt.getSubject())
        );
    }
}
