package br.com.avia.avia_api.dto.sprint;

public record EditarSprintRequestDTO(
        String nome,
        String status,
        String dataInicio,
        String dataFim
) {
}
