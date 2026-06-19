package br.com.avia.avia_api.dto.projeto;

import br.com.avia.avia_api.enums.CargoProjeto;

public record MembroResponseDTO(
        long id,
        String nome,
        String email,
        CargoProjeto cargo
) {
}
