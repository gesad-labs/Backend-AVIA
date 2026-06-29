package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Papel;
import br.com.avia.avia_api.entity.Projeto;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ProjetoUsuarioRepository extends JpaRepository<ProjetoUsuario, Long> {

    @Query("SELECT pu.projeto FROM ProjetoUsuario pu WHERE pu.usuario.id = :usuarioId")
    List<Projeto> findProjetosByMembroId(@Param("usuarioId") Long usuarioId);

    @Query("SELECT pu FROM ProjetoUsuario pu JOIN FETCH pu.usuario JOIN FETCH pu.papel WHERE pu.projeto.id = :projetoId")
    List<ProjetoUsuario> findMembrosByProjetoId(@Param("projetoId") Long projetoId);

    @Query("SELECT pu FROM ProjetoUsuario pu JOIN FETCH pu.papel WHERE pu.projeto.id = :projetoId AND pu.usuario.id = :usuarioId")
    Optional<ProjetoUsuario> findByProjetoIdAndUsuarioId(@Param("projetoId") Long projetoId, @Param("usuarioId") Long usuarioId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProjetoUsuario pu WHERE pu.projeto.id = :projetoId")
    void deleteByProjetoId(@Param("projetoId") Long projetoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProjetoUsuario pu WHERE pu.projeto.id = :projetoId AND pu.usuario.id = :usuarioId")
    void deleteByProjetoIdAndUsuarioId(@Param("projetoId") Long projetoId, @Param("usuarioId") Long usuarioId);

    boolean existsByProjetoIdAndUsuarioId(Long projetoId, Long usuarioId);
}
