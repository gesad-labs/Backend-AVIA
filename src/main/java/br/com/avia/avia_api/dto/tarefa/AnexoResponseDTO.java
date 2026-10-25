package br.com.avia.avia_api.dto.tarefa;

import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.enums.TipoAnexo;
import java.time.LocalDateTime;

public record AnexoResponseDTO(
        Long id,
        Long tarefaId,
        TipoAnexo tipoAnexo,
        Long tamanhoBytes,
        LocalDateTime criadoEm,
        String nomeArquivo,
        String nomeUsuario,
        //uso para o front saber se deve mostrar o botao de remover ou nao :)
        Boolean podeRemover
) {}