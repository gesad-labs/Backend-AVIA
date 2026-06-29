package br.com.avia.avia_api.controller;

import br.com.avia.avia_api.dto.notificacao.NotificacaoResponseDTO;
import br.com.avia.avia_api.service.NotificacaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projetos/{projetoId}/notificacoes")
@Tag(name = "Notificacao", description = "Gerenciamento de notificação")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public List<NotificacaoResponseDTO> listarNotificacoesDoProjeto(
            @PathVariable Long projetoId,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();

        return notificacaoService.listarNotificacoesDoProjeto(projetoId, emailUsuario);
    }

    @GetMapping("/nao-lidas")
    public Long contarNaoLidasDoProjeto(
            @PathVariable Long projetoId,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();

        return notificacaoService.contarNaoLidasDoProjeto(projetoId, emailUsuario);
    }

    @PatchMapping("/{notificacaoId}/lida")
    public void marcarComoLidaNoProjeto(
            @PathVariable Long projetoId,
            @PathVariable Long notificacaoId,
            Authentication authentication
    ) {
        String emailUsuario = authentication.getName();

        notificacaoService.marcarComoLidaNoProjeto(projetoId, notificacaoId, emailUsuario);
    }
}