package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.enums.TipoAnexo;
import org.springframework.core.io.Resource;

public record BaixarAnexoDTO(Resource recurso, String nome, TipoAnexo tipo){}
