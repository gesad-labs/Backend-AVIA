package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.planningPoker.PlanningPokerEventoDTO;
import br.com.avia.avia_api.dto.planningPoker.PlanningPokerSessaoResponseDTO;
import br.com.avia.avia_api.enums.TipoEventoPlanningPoker;
import br.com.avia.avia_api.service.PlanningPokerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projetos/{projetoId}/tarefas/{tarefaId}/planning-poker")
@Tag(name = "Planning Poker", description = "Estimativa colaborativa de tarefas")
public class PlanningPokerController {

    private final PlanningPokerService planningPokerService;
    private final SimpMessagingTemplate messagingTemplate;

    public PlanningPokerController(
            PlanningPokerService planningPokerService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.planningPokerService = planningPokerService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/sessoes")
    public ResponseEntity<PlanningPokerSessaoResponseDTO> criarSessao(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        PlanningPokerSessaoResponseDTO sessao =
                planningPokerService.criarSessao(projetoId, tarefaId, jwt.getSubject());

        publicarNoProjeto(TipoEventoPlanningPoker.SESSAO_CRIADA, sessao);
        publicarNaTarefa(TipoEventoPlanningPoker.SESSAO_CRIADA, sessao);

        return new ResponseEntity<>(sessao, HttpStatus.CREATED);
    }

    @GetMapping("/sessao-ativa")
    public ResponseEntity<PlanningPokerSessaoResponseDTO> buscarSessaoAtiva(
            @PathVariable Long projetoId,
            @PathVariable Long tarefaId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return planningPokerService.buscarSessaoAtiva(projetoId, tarefaId, jwt.getSubject())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private void publicarNoProjeto(TipoEventoPlanningPoker tipo, PlanningPokerSessaoResponseDTO sessao) {
        messagingTemplate.convertAndSend(
                "/topic/projetos/" + sessao.projetoId() + "/planning-poker",
                new PlanningPokerEventoDTO(tipo, sessao)
        );
    }

    private void publicarNaTarefa(TipoEventoPlanningPoker tipo, PlanningPokerSessaoResponseDTO sessao) {
        messagingTemplate.convertAndSend(
                "/topic/projetos/" + sessao.projetoId()
                        + "/tarefas/" + sessao.tarefaId()
                        + "/planning-poker",
                new PlanningPokerEventoDTO(tipo, sessao)
        );
    }
}