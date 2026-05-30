package com.example.springApp.repository;

import com.example.springApp.model.Draw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    /**
     * Lista todos os pares sorteados de um grupo.
     */
    List<Draw> findByGrupoId(Long grupoId);

    // Retorna as duas relacoes do usuario no ciclo: quem ele tirou e quem tirou ele.
    @Query("""
            select draw from Draw draw
            join fetch draw.grupo grupo
            join fetch draw.remetente remetente
            join fetch draw.destinatario destinatario
            where draw.grupo.id = :groupId
              and (draw.remetente.id = :userId or draw.destinatario.id = :userId)
            """)
    List<Draw> findUserDrawRelations(@Param("groupId") Long groupId, @Param("userId") Long userId);

    /**
     * Indica se o grupo ja teve sorteio realizado.
     */
    boolean existsByGrupoId(Long grupoId);

    /**
     * Remove sorteios antigos quando o grupo e excluido ou sorteado novamente.
     */
    void deleteByGrupoId(Long grupoId);

    // Usado para consultar o resultado individual sem expor o sorteio completo.
    @Query("""
            select draw from Draw draw
            join fetch draw.grupo grupo
            join fetch draw.remetente remetente
            join fetch draw.destinatario destinatario
            where grupo.id = :groupId
              and remetente.id = :giverId
            """)
    Optional<Draw> findByGrupo_IdAndRemetente_Id(@Param("groupId") Long groupId, @Param("giverId") Long giverId);

    /**
     * Localiza quem tirou determinado usuario dentro do grupo.
     */
    Optional<Draw> findByGrupo_IdAndDestinatario_Id(Long groupId, Long receiverId);

    /**
     * Encontra todos os sorteios em que o usuario aparece como presenteado.
     */
    @Query("""
            select draw from Draw draw
            join fetch draw.grupo grupo
            join fetch draw.remetente remetente
            join fetch draw.destinatario destinatario
            where destinatario.id = :receiverId
            """)
    List<Draw> findByDestinatario_Id(@Param("receiverId") Long receiverId);

    /**
     * Verifica se dois usuarios formam um par direto permitido para chat ou wishlist.
     */
    boolean existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(Long groupId, Long giverId, Long receiverId);

    /**
     * Verifica o par direto em qualquer direcao com uma unica ida ao banco.
     */
    // Uma unica consulta cobre os dois sentidos do par e evita duas idas ao banco.
    @Query("""
            select count(draw) > 0 from Draw draw
            where draw.grupo.id = :groupId
              and ((draw.remetente.id = :userId and draw.destinatario.id = :otherUserId)
                or (draw.remetente.id = :otherUserId and draw.destinatario.id = :userId))
            """)
    boolean existsDirectPairInEitherDirection(
            @Param("groupId") Long groupId,
            @Param("userId") Long userId,
            @Param("otherUserId") Long otherUserId
    );

}
