package br.com.avia.avia_api.dto.projeto;

import br.com.avia.avia_api.enums.CargoProjeto;
import br.com.avia.avia_api.enums.PapelProjeto;

public record AdicionarMembroProjetoRequestDTO(

        String emailUsuario,
        CargoProjeto cargo,
        PapelProjeto papel
) {
}
