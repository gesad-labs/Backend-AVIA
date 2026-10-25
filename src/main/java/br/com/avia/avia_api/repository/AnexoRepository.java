package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Anexo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    // como eu uso o usuario que enviou o anexo para fazer o DTO, como ele é LAZY o banco so traz um proxy,
    // entao sempre que a consulta de algum parametro alem do ID é feita, ele faz um SELECT no banco, o EntityGraph impede isso
    // e traz o objeto junto com o anexo
    @EntityGraph(attributePaths = "enviadoPor")
    List<Anexo> findByTarefaIdOrderByCriadoEmAsc(Long tarefaId);
    Optional<Anexo> findByIdAndTarefaId(Long anexoid, Long tarefaId);

    int countByTarefaId(long tarefaId);
}