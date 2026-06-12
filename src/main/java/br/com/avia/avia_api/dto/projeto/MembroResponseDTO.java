package br.com.avia.avia_api.dto.projeto;

public record MembroResponseDTO(
        long id,
        String nome,
        String email,
        String cargo
) {
}
