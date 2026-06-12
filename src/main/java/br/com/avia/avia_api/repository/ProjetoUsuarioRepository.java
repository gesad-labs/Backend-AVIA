package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Projeto;
import br.com.avia.avia_api.entity.ProjetoUsuario;
import br.com.avia.avia_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProjetoUsuarioRepository extends JpaRepository<ProjetoUsuario, Long> {
    // Lista os projetos do usuário
    @Query("SELECT pu.projeto FROM ProjetoUsuario pu WHERE pu.usuario.id = :usuarioId")
    List<Projeto> findProjetosByMembroId(@Param("usuarioId") Long usuarioId);

    // Lista os membros de um projeto
    @Query("SELECT pu FROM ProjetoUsuario pu JOIN FETCH pu.usuario WHERE pu.projeto.id = :projetoId")
    List<ProjetoUsuario> findMembrosByProjetoId(@Param("projetoId") Long projetoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProjetoUsuario pu WHERE pu.projeto.id = :projetoId")
    void deleteByProjetoId(@Param("projetoId") Long projetoId);

    @Modifying
    @Transactional
    @Query("DELETE FROM ProjetoUsuario pu WHERE pu.usuario.id = :usuarioId")
    void deleteByUsuarioId(@Param("usuarioId") Long usuarioId);

    boolean existsByProjetoIdAndUsuarioId(Long projetoId, Long usuarioId);
}
