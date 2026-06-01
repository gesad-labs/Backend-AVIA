package br.com.avia.avia_api.dto.usuario;

public record AtualizarUsuarioRequestDTO(
        String nome,
        String senhaAtual,
        String novaSenha
) {
}