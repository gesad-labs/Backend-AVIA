package br.com.avia.avia_api.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record RedefinicaoSenhaRequestDTO(

        @NotBlank(message = "O token é obrigatório")
        String token,

        @NotBlank(message = "A nova senha é obrigatória")
        String novaSenha
) {
}