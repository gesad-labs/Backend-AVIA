package br.com.avia.avia_api.dto.projeto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RemoverMembroRequestDTO(
        @NotBlank(message = "O e-mail do membro é obrigatório")
        @Email(message = "E-mail inválido")
        String emailMembro
) {
}
