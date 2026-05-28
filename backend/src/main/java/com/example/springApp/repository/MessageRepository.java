package com.example.springApp.repository;

import com.example.springApp.dto.UnreadConversationCount;
import com.example.springApp.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Busca mensagens recebidas por usuario em um grupo.
     */
    List<Message> findByGrupoIdAndDestinatarioId(Long groupId, Long receiverId);

    /**
     * Calcula o badge global de mensagens nao lidas.
     */
    Long countByDestinatarioIdAndLidaFalse(Long userId);

    /**
     * Conta nao lidas de uma conversa especifica.
     */
    Long countByGrupoIdAndRemetenteIdAndDestinatarioIdAndLidaFalse(Long groupId, Long senderId, Long receiverId);

    /**
     * Limpa mensagens vinculadas a um grupo excluido ou sorteado novamente.
     */
    void deleteByGrupoId(Long groupId);

    /**
     * Retorna conversa bilateral em ordem cronologica.
     */
    // Fetch joins evitam consultas extras no ResponseMapper ao acessar grupo/remetente/destinatario.
    @Query("""
            select m from Message m
            join fetch m.grupo
            join fetch m.remetente
            join fetch m.destinatario
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

    /**
     * Conta mensagens nao lidas por conversa em uma unica consulta para evitar N+1 no resumo de chats.
     */
    @Query("""
            select m.remetente.id as otherUserId, count(m) as unreadCount
            from Message m
            where m.grupo.id = :groupId
              and m.destinatario.id = :userId
              and m.remetente.id in :otherUserIds
              and m.lida = false
            group by m.remetente.id
            """)
    List<UnreadConversationCount> countUnreadByConversationPartners(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("otherUserIds") List<Long> otherUserIds
    );

    /**
     * Marca como lidas apenas mensagens recebidas nessa conversa, sem carregar o historico completo.
     */
    @Modifying
    @Query("""
            update Message m
            set m.lida = true
            where m.grupo.id = :groupId
              and m.destinatario.id = :userId
              and m.remetente.id = :otherUserId
              and m.lida = false
            """)
    int markConversationReceivedMessagesAsRead(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("otherUserId") Long otherUserId
    );

    /**
     * Lista todas as mensagens de um grupo em ordem de envio.
     */
    List<Message> findByGrupo_IdOrderByDataEnvioAsc(Long grupoId);

}
