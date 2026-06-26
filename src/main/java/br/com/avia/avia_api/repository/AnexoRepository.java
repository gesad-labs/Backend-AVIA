package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Integer> {

    List<Anexo> findByTarefaId(long tarefaId);

}