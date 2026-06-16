package br.com.avia.avia_api.dto.projeto;

import br.com.avia.avia_api.enums.CargoProjeto;
import jakarta.validation.constraints.NotNull;

public record AdicionarMembroProjetoRequestDTO(

        String emailUsuario,
        CargoProjeto cargo
) {
}
