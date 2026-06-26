package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.NotBlank;

public record CriarAnexoRequestDTO(
        @NotBlank String nome,
        @NotBlank String tipoArquivo,
        @NotBlank String url
) {}