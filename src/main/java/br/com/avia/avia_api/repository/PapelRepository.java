package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Papel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PapelRepository extends JpaRepository<Papel, Long> {
    Optional<Papel> findByNome(String nome);
}
