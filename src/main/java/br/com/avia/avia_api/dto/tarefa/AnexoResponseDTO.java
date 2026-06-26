package br.com.avia.avia_api.dto.tarefa;

public record AnexoResponseDTO(
        int id,
        long tarefaId,
        String nome,
        String tipoArquivo,
        String url
) {}