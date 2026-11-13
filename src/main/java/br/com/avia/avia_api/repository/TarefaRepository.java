package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Tarefa;
import br.com.avia.avia_api.enums.StatusSprint;
import br.com.avia.avia_api.enums.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {

    List<Tarefa> findBySprintId(long sprintId);

    long countBySprintIdAndStatusNot(Long sprintId, StatusTarefa status);

    List<Tarefa> findByProjetoIdAndSprintId(long projetoId, long sprintId);

    List<Tarefa> findByProjetoIdAndSprintIdIsNull(Long projetoId);

    @Query("select t from Tarefa t where t.projetoId = :projetoId and (t.sprintId is null or t.sprintId in " +
            "(select s.id from Sprint s where s.projetoId = :projetoId and s.status = :statusSprint))")
    List<Tarefa> buscarBacklogComSprintsNoStatus(@Param("projetoId") Long projetoId,
                                                 @Param("statusSprint") StatusSprint statusSprint);

    List<Tarefa> findByProjetoId(Long projetoId);

    Optional<Tarefa> findByIdAndProjetoId(Long tarefaId, Long projetoId);

    int countByTarefaParenteId(Long tarefaParenteId);

    int countByTarefaParenteIdAndStatus(Long tarefaParenteId, StatusTarefa status);

    List<Tarefa> findByTarefaParenteId(Long tarefaParenteId);

    boolean existsByTarefaParenteId(Long tarefaParenteId);

    boolean existsByTarefaParenteIdAndStatusNot(Long tarefaParenteId, StatusTarefa status);

    List<Tarefa> findByBloqueadaPorId(Long bloqueadaPorId);
}