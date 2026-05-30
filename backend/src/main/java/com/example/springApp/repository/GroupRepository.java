package com.example.springApp.repository;

import com.example.springApp.dto.GroupSummary;
import com.example.springApp.model.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Localiza grupo pelo codigo compartilhado com convidados.
     */
    Optional<Group> findByCodigoUnico(String codigoUnico);

    /**
     * Impede que um usuario crie mais de um grupo como dono.
     */
    boolean existsByDonoId(Long donoId);

    /**
     * Verifica participacao sem carregar a entidade e seus relacionamentos.
     */
    boolean existsByIdAndMembros_Id(Long groupId, Long userId);

    /**
     * Carrega os grupos do usuario com dono e membros para evitar consultas adicionais na resposta.
     */
    @EntityGraph(attributePaths = {"dono", "membros"})
    List<Group> findByMembros_Id(Long usuarioId);

    /**
     * Lista grupos do usuario sem carregar todos os membros; suficiente para cards/resumos.
     */
    // Conta todos os membros sem materializar a colecao de usuarios na JVM.
    @Query("""
            select new com.example.springApp.dto.GroupSummary(groupEntity, count(member))
            from Group groupEntity
            join groupEntity.membros currentUser
            left join groupEntity.membros member
            where currentUser.id = :usuarioId
            group by groupEntity
            """)
    List<GroupSummary> findSummariesByMembros_Id(Long usuarioId);

    /**
     * Garante que detalhes do grupo so sejam retornados para participantes.
     */
    @EntityGraph(attributePaths = {"dono", "membros"})
    Optional<Group> findByIdAndMembros_Id(Long groupId, Long userId);

}
