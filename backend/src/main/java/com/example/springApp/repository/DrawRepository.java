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

    List<Draw> findByGrupoId(Long grupoId);

    @Query("""
            select draw from Draw draw
            where draw.grupo.id = :groupId
              and (draw.remetente.id = :userId or draw.destinatario.id = :userId)
            """)
    List<Draw> findUserDrawRelations(@Param("groupId") Long groupId, @Param("userId") Long userId);

    boolean existsByGrupoId(Long grupoId);
    void deleteByGrupoId(Long grupoId);

    // Para o usuario ver quem ele tirou em um grupo especifico
    Optional<Draw> findByGrupo_IdAndRemetente_Id(Long groupId, Long giverId);
    Optional<Draw> findByGrupo_IdAndDestinatario_Id(Long groupId, Long receiverId);
    boolean existsByGrupo_IdAndRemetente_IdAndDestinatario_Id(Long groupId, Long giverId, Long receiverId);

}
