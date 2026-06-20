package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
}