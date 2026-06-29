package br.com.avia.avia_api.dto.sprint;

import br.com.avia.avia_api.dto.tarefa.TarefaResponseDTO;

import java.util.List;

public record BacklogResponseDTO(Long backlogSprintId, List<TarefaResponseDTO> tarefas) {
}
