package br.com.avia.avia_api.dto.tarefa;

public record TarefaResponseDTO(
        long id,
        long sprintId,
        String titulo,
        int estimativa,
        String status,
        Integer tarefaParenteId,
        boolean impedimento,
        boolean estimativaConsolidada,
        String prioridade,
        String tipo
) {
}
