package br.com.avia.avia_api.dto.tarefa;

public record TarefaResponseDTO(
        long id,
        long sprintId,
        String titulo,
        String descricao,
        int estimativa,
        String status,
        String prioridade,
        Integer tarefaParenteId,
        boolean impedimento
) {}