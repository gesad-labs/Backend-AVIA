package br.com.avia.avia_api.repository;

import br.com.avia.avia_api.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
}