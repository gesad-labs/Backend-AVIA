package br.com.avia.avia_api.dto.tarefa;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AtribuirResponsavelRequestDTO(

        @NotEmpty(message = "Lista de responsáveis não pode ser vazia")
        List<Long> projetoUsuarioIds

) {}