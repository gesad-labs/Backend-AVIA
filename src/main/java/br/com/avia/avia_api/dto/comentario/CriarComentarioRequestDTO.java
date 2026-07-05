package br.com.avia.avia_api.dto.comentario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CriarComentarioRequestDTO(
        @NotBlank(message = "A descrição é obrigatória")
        String descricao,
        List<@Email(message = "E-mail mencionado inválido") String> emailsMencionados
) {
}
