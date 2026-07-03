package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.PrioridadeTarefa;
import br.com.avia.avia_api.enums.StatusTarefa;
import java.util.List;

public record TarefaResponseDTO(
        Long id,
        Long sprintId,
        String titulo,
        String descricao,
        Integer estimativa,
        StatusTarefa status,
        PrioridadeTarefa prioridade,
        Long tarefaParenteId,
        Boolean impedimento,
        List<ResponsavelDTO> responsaveis,
        String tipo,
        Integer subtarefasCount,
        Integer anexosCount
) {}
