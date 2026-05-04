package com.example.springApp.repository;

import com.example.springApp.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUsuarioOrderByDataCriacaoDesc(Long userId);

}