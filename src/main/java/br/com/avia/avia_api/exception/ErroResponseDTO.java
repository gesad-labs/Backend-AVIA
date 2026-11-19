package br.com.avia.avia_api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponseDTO(String mensagem, Map<String, List<String>> erros) {

    public ErroResponseDTO(String mensagem){
        this(mensagem, null);
    }
}