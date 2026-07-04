package br.com.avia.avia_api.dto.comentario;

import java.time.LocalDateTime;

public record ComentarioResponseDTO(
        Long id,
        Long tarefaId,
        String descricao,
        Long projetoUsuarioId,
        boolean impedimento,
        LocalDateTime criadoEm
) {}
