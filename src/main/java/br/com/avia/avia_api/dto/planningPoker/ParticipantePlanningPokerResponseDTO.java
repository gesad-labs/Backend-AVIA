package br.com.avia.avia_api.dto.planningPoker;

public record ParticipantePlanningPokerResponseDTO(
        Long usuarioId,
        String nome,
        Boolean votou,
        Integer valor
) {
}