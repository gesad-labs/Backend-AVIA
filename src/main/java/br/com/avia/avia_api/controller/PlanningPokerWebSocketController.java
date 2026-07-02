package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.planningPoker.FinalizarPlanningPokerRequestDTO;
import br.com.avia.avia_api.dto.planningPoker.PlanningPokerEventoDTO;
import br.com.avia.avia_api.dto.planningPoker.PlanningPokerSessaoResponseDTO;
import br.com.avia.avia_api.dto.planningPoker.VotarPlanningPokerRequestDTO;
import br.com.avia.avia_api.enums.TipoEventoPlanningPoker;
import br.com.avia.avia_api.exception.permissao.PermissaoNegadaException;
import br.com.avia.avia_api.service.PlanningPokerService;
import jakarta.validation.Valid;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class PlanningPokerWebSocketController {

    private final PlanningPokerService planningPokerService;
    private final SimpMessagingTemplate messagingTemplate;

    public PlanningPokerWebSocketController(
            PlanningPokerService planningPokerService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.planningPokerService = planningPokerService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/planning-poker/{sessaoId}/votar")
    public void votar(
            @DestinationVariable Long sessaoId,
            @Payload @Valid VotarPlanningPokerRequestDTO dto,
            Principal principal
    ) {
        PlanningPokerSessaoResponseDTO sessao =
                planningPokerService.votar(sessaoId, dto.valor(), email(principal));

        publicarNaTarefa(TipoEventoPlanningPoker.VOTO_REGISTRADO, sessao);
    }

    @MessageMapping("/planning-poker/{sessaoId}/revelar")
    public void revelar(
            @DestinationVariable Long sessaoId,
            Principal principal
    ) {
        PlanningPokerSessaoResponseDTO sessao =
                planningPokerService.revelar(sessaoId, email(principal));

        publicarNaTarefa(TipoEventoPlanningPoker.VOTOS_REVELADOS, sessao);
    }

    @MessageMapping("/planning-poker/{sessaoId}/nova-rodada")
    public void novaRodada(
            @DestinationVariable Long sessaoId,
            Principal principal
    ) {
        PlanningPokerSessaoResponseDTO sessao =
                planningPokerService.novaRodada(sessaoId, email(principal));

        publicarNaTarefa(TipoEventoPlanningPoker.NOVA_RODADA, sessao);
    }

    @MessageMapping("/planning-poker/{sessaoId}/finalizar")
    public void finalizar(
            @DestinationVariable Long sessaoId,
            @Payload @Valid FinalizarPlanningPokerRequestDTO dto,
            Principal principal
    ) {
        PlanningPokerSessaoResponseDTO sessao =
                planningPokerService.finalizar(sessaoId, dto.estimativaFinal(), email(principal));

        publicarNaTarefa(TipoEventoPlanningPoker.SESSAO_FINALIZADA, sessao);
    }

    @MessageMapping("/planning-poker/{sessaoId}/cancelar")
    public void cancelar(
            @DestinationVariable Long sessaoId,
            Principal principal
    ) {
        PlanningPokerSessaoResponseDTO sessao =
                planningPokerService.cancelar(sessaoId, email(principal));

        publicarNaTarefa(TipoEventoPlanningPoker.SESSAO_CANCELADA, sessao);
    }

    private String email(Principal principal) {
        if (principal == null) {
            throw new PermissaoNegadaException();
        }

        return principal.getName();
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