package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.TarefaProjetoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaProjetoUsuarioRepository extends JpaRepository<TarefaProjetoUsuario, Long> {

    List<TarefaProjetoUsuario> findByTarefaId(Long tarefaId);

    void deleteByTarefaId(Long tarefaId);
}
