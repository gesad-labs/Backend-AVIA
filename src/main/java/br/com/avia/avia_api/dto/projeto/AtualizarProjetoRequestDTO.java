package br.com.avia.avia_api.dto.projeto;

import jakarta.validation.constraints.Size;

public record AtualizarProjetoRequestDTO(
        @Size(max = 255, message = "O nome não pode passar de 255 caracteres")
        String nome
) {}