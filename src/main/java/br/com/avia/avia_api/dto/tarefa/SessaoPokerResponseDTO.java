package br.com.avia.avia_api.dto.tarefa;

import java.util.List;

public record SessaoPokerResponseDTO(
        Long tarefaId,
        List<VotoResponseDTO> votos,
        Integer mediaVotos,
        boolean estimativaConsolidada,
        int estimativaFinal
) {
}
