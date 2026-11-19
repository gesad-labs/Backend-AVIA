package br.com.avia.avia_api.dto.permissao;

import br.com.avia.avia_api.enums.PermissoesUsuarioProjeto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PermissoesMembroRequestDTO(
        @NotNull(message = "A lista de permissões é obrigatória")
        List<@NotNull(message = "Permissão inválida") PermissoesUsuarioProjeto> permissoes
) {
}
