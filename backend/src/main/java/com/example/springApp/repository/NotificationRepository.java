package com.example.springApp.repository;

import com.example.springApp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUsuarioIdOrderByDataCriacaoDesc(Long userId);
    List<Notification> findByUsuarioIdAndLidaFalse(Long userId);
    Optional<Notification> findByIdAndUsuarioId(Long id, Long userId);
    long countByUsuarioIdAndLidaFalse(Long userId);
    void deleteByUsuarioId(Long userId);

}
