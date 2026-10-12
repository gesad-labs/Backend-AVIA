package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.enums.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findBySprintId(long sprintId);

    List<Tarefa> findByProjetoIdAndSprintId(long projetoId, long sprintId);

    List<Tarefa> findByProjetoIdAndSprintIdIsNull(Long projetoId);

    List<Tarefa> findByProjetoId(Long projetoId);

    int countByTarefaParenteId(Long tarefaParenteId);

    int countByTarefaParenteIdAndStatus(Long tarefaParenteId, StatusTarefa status);

    List<Tarefa> findByTarefaParenteId(Long tarefaParenteId);

    boolean existsByTarefaParenteId(Long tarefaParenteId);

    boolean existsByTarefaParenteIdAndStatusNot(Long tarefaParenteId, StatusTarefa status);
}