package com.example.springApp.repository;

import com.example.springApp.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Lista notificacoes recentes primeiro para o painel do usuario.
     */
    // O mapper serializa o usuario; EntityGraph evita uma consulta extra por notificacao.
    @EntityGraph(attributePaths = "usuario")
    List<Notification> findByUsuarioIdOrderByDataCriacaoDesc(Long userId);

    /**
     * Lista notificacoes com limite para evitar payloads e listas sem controle.
     */
    @EntityGraph(attributePaths = "usuario")
    List<Notification> findByUsuarioIdOrderByDataCriacaoDesc(Long userId, Pageable pageable);

    /**
     * Carrega apenas notificacoes pendentes para marcacao em lote.
     */
    List<Notification> findByUsuarioIdAndLidaFalse(Long userId);

    /**
     * Busca notificacao com escopo de usuario para impedir acesso cruzado.
     */
    @EntityGraph(attributePaths = "usuario")
    Optional<Notification> findByIdAndUsuarioId(Long id, Long userId);

    /**
     * Marca notificacoes pendentes sem carregar cada entidade em memoria.
     */
    @Modifying
    @Query("update Notification notification set notification.lida = true where notification.usuario.id = :userId and notification.lida = false")
    int markAllUnreadAsRead(Long userId);

    /**
     * Conta notificacoes pendentes sem carregar entidades.
     */
    long countByUsuarioIdAndLidaFalse(Long userId);

    /**
     * Remove todas as notificacoes de um usuario.
     */
    void deleteByUsuarioId(Long userId);

}
