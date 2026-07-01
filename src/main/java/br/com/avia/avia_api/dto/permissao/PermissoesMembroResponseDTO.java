package br.com.avia.avia_api.dto.permissao;

import br.com.avia.avia_api.enums.PermissoesUsuarioProjeto;

import java.util.List;

public record PermissoesMembroResponseDTO(
        List<PermissoesUsuarioProjeto> permissoes
) {
}
