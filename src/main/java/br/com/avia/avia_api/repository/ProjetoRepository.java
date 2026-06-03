package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {
        @Query("SELECT p FROM Projeto p JOIN p.membros pu WHERE pu.usuario.id = :usuarioId")
        List<Projeto> findProjetosByMembroId(@Param("usuarioId") Long usuarioId);
}

