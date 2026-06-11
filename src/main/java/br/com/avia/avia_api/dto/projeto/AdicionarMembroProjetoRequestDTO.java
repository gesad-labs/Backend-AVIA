package br.com.avia.avia_api.dto.projeto;

public record AdicionarMembroProjetoRequestDTO(
        String emailUsuario,
        String cargo
) {
}
