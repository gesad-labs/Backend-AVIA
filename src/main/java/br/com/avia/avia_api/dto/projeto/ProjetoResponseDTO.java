package br.com.avia.avia_api.dto.projeto;

public record ProjetoResponseDTO(
        Long id,
        String nome,
        String tipoEstimativa
) {
}
