package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record VotoRequestDTO(
        @NotNull(message = "O valor do voto é obrigatório")
        @Min(value = 0, message = "O valor deve ser maior ou igual a zero")
        Integer valor
) {
}
