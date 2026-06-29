package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
    List<Tarefa> findBySprintId(Long sprintId);
}