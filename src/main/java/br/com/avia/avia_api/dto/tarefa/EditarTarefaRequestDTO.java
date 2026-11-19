package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.PrioridadeTarefa;
import java.util.List;

public record EditarTarefaRequestDTO(
        String titulo,
        String descricao,
        Integer estimativa,
        PrioridadeTarefa prioridade,
        List<Long> responsaveisIds,
        Long tarefaParenteId,
        String tipo,
        Long bloqueadaPorId
) {}
