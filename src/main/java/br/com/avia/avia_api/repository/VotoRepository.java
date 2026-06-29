package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    @Query("SELECT v FROM Voto v JOIN FETCH v.usuario WHERE v.tarefaId = :tarefaId")
    List<Voto> findByTarefaId(@Param("tarefaId") Long tarefaId);

    @Query("SELECT v FROM Voto v WHERE v.tarefaId = :tarefaId AND v.usuario.id = :usuarioId")
    Optional<Voto> findByTarefaIdAndUsuarioId(@Param("tarefaId") Long tarefaId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Voto v WHERE v.tarefaId = :tarefaId")
    void deleteByTarefaId(@Param("tarefaId") Long tarefaId);
}
