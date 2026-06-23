package br.com.avia.avia_api.dto.tarefa;

public record EditarTarefaRequestDTO(

        String titulo,
        String descricao,
        Integer estimativa,
        String prioridade

) {}