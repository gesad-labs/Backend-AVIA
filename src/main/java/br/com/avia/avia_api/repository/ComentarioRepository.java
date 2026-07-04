package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByTarefaIdOrderByIdAsc(Long tarefaId);
}