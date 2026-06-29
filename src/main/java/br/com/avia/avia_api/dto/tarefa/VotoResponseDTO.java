package br.com.avia.avia_api.dto.tarefa;

public record VotoResponseDTO(
        Long id,
        Long usuarioId,
        String nomeUsuario,
        int valor
) {
}
