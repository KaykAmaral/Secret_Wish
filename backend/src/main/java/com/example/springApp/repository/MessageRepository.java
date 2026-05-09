package com.example.springApp.repository;

import com.example.springApp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByGrupoIdAndDestinatarioId(Long groupId, Long receiverId);
    Long countByDestinatarioIdAndLidaFalse(Long userId);
    void deleteByGrupoId(Long groupId);

    @Query("""
            select m from Message m
            where m.grupo.id = :groupId
              and ((m.remetente.id = :userId and m.destinatario.id = :otherUserId)
                or (m.remetente.id = :otherUserId and m.destinatario.id = :userId))
            order by m.dataEnvio asc
            """)
    List<Message> findConversation(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("otherUserId") Long otherUserId
    );

    List<Message> findByGrupo_IdOrderByDataEnvioAsc(Long grupoId);

}
