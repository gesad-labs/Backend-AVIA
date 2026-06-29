package br.com.avia.avia_api.dto.tarefa;

public record AtualizarTarefaRequestDTO(
        String titulo,
        Integer estimativa,
        String status,
        String prioridade,
        String tipo,
        boolean impedimento
) {
}
