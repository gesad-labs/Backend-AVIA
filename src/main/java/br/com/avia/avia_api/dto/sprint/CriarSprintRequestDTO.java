package br.com.avia.avia_api.dto.sprint;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarSprintRequestDTO(
        @NotBlank(message = "O nome da sprint é obrigatório")
        @Size(max = 255, message = "O nome não pode passar de 255 caracteres")
        String nome,
        String dataInicio,
        String dataFim
) {}
