package br.com.avia.avia_api.dto.projeto;

import br.com.avia.avia_api.enums.TipoEstimativaProjeto;

public record ProjetoResponseDTO(
        Long id,
        String nome,
        TipoEstimativaProjeto tipoEstimativa
) {
}
