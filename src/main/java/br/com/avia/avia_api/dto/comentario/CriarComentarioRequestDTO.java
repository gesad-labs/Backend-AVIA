package br.com.avia.avia_api.dto.comentario;

import jakarta.validation.constraints.NotBlank;

public record CriarComentarioRequestDTO(
        @NotBlank String descricao
) {}
