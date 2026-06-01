package br.com.avia.avia_api.dto.usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email
) {
}
