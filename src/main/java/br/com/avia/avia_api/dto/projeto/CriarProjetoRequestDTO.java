package br.com.avia.avia_api.dto.projeto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarProjetoRequestDTO(
        @NotBlank(message = "O nome do projeto é obrigatório")
        @Size(max = 255, message = "O nome não pode passar de 255 caracteres")
        String nome,

        @NotBlank(message = "O tipo de estimativa é obrigatório")
        @Size(max = 255, message = "O tipo de estimativa não pode passar de 255 caracteres")
        String tipoEstimativa
) {

}
