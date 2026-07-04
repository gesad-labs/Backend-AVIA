package br.com.avia.avia_api.dto.comentario;

import jakarta.validation.constraints.NotBlank;

public record EditarComentarioRequestDTO(
        @NotBlank(message = "A descrição é obrigatória")
        String descricao
) {
}