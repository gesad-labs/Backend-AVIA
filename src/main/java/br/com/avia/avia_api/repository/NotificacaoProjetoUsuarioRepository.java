package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.NotificacaoProjetoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificacaoProjetoUsuarioRepository extends JpaRepository<NotificacaoProjetoUsuario, Long> {

    List<NotificacaoProjetoUsuario>
    findByProjetoUsuarioProjetoIdAndProjetoUsuarioUsuarioEmailOrderByNotificacaoCriadoEmDesc(
            Long projetoId,
            String emailUsuario
    );

    Long countByProjetoUsuarioProjetoIdAndProjetoUsuarioUsuarioEmailAndLidaFalse(
            Long projetoId,
            String emailUsuario
    );

    Optional<NotificacaoProjetoUsuario>
    findByIdAndProjetoUsuarioProjetoIdAndProjetoUsuarioUsuarioEmail(
            Long id,
            Long projetoId,
            String emailUsuario
    );
}