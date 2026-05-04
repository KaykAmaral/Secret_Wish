package com.example.springApp.repository;

import com.example.springApp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByGrupoIdAndDestinatarioId(Long groupId, Long receiverId);
    Long countByDestinatarioIdAndLidaFalse(Long userId);

}