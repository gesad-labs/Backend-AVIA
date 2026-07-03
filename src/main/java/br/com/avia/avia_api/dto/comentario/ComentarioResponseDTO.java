package br.com.avia.avia_api.dto.comentario;

public record ComentarioResponseDTO(
        long id,
        long tarefaId,
        String descricao,
        int projetoUsuarioId,
        boolean impedimento
) {
}