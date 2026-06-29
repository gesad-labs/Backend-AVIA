package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusTarefaRequestDTO(

        @NotNull(message = "O status é obrigatório")
        StatusTarefa status
) {}