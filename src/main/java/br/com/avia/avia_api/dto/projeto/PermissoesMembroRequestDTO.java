package br.com.avia.avia_api.dto.projeto;

import br.com.avia.avia_api.enums.PermissoesUsuarioProjeto;

import java.util.List;

public record PermissoesMembroRequestDTO(List<PermissoesUsuarioProjeto> permissoes) {}
