package br.com.avia.avia_api.dto.projeto;

import br.com.avia.avia_api.enums.CargoProjeto;
import br.com.avia.avia_api.enums.PapelProjeto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdicionarMembroProjetoRequestDTO(

        @NotBlank(message = "O e-mail do usuário é obrigatório")
        @Email(message = "E-mail inválido")
        String emailUsuario,

        @NotNull(message = "O cargo é obrigatório")
        CargoProjeto cargo,

        @NotNull(message = "O papel é obrigatório")
        PapelProjeto papel
) {
}
