package com.example.springApp.repository;

import com.example.springApp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lista notificacoes recentes primeiro para o painel do usuario.
     */
    List<Notification> findByUsuarioIdOrderByDataCriacaoDesc(Long userId);

    /**
     * Carrega apenas notificacoes pendentes para marcacao em lote.
     */
    List<Notification> findByUsuarioIdAndLidaFalse(Long userId);

    /**
     * Busca notificacao com escopo de usuario para impedir acesso cruzado.
     */
    Optional<Notification> findByIdAndUsuarioId(Long id, Long userId);

    /**
     * Conta notificacoes pendentes sem carregar entidades.
     */
    long countByUsuarioIdAndLidaFalse(Long userId);

    /**
     * Remove todas as notificacoes de um usuario.
     */
    void deleteByUsuarioId(Long userId);

}
