package br.com.avia.avia_api.dto.comentario;

import java.time.LocalDateTime;

public record ComentarioResponseDTO(
        Long id,
        Long tarefaId,
        String descricao,
        String autorNome,
        String autorEmail,
        LocalDateTime criadoEm
) {}
