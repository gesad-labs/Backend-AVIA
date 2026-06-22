package br.com.avia.avia_api.dto.sprint;

public record SprintResponseDTO(
        Long id,
        String nome,
        Long projetoId,
        String status
) {}

