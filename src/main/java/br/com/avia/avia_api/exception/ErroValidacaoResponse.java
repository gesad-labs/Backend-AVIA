package br.com.avia.avia_api.exception;

import java.util.List;
import java.util.Map;

public record ErroValidacaoResponse(String mensagem, Map<String, List<String>> erros) {
}
