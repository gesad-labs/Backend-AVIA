package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.StatusTarefa;
import java.util.List;

public record FiltroHistoricoDTO(
        List<Long> responsaveisIds,
        Long sprintInicioId,
        Long sprintFimId,
        List<StatusTarefa> status,
        Boolean impedimento
) {}
