package br.com.avia.avia_api.dto.notificacao;

import br.com.avia.avia_api.enums.TipoNotificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        TipoNotificacao tipo,
        String mensagem,
        String nomeProjeto,
        Boolean lida,
        LocalDateTime criadoEm
) {
}