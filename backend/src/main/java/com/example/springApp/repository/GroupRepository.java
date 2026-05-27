package com.example.springApp.repository;

import com.example.springApp.model.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
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
     * Carrega os grupos do usuario com dono e membros para evitar consultas adicionais na resposta.
     */
    @EntityGraph(attributePaths = {"dono", "membros"})
    List<Group> findByMembros_Id(Long usuarioId);

    /**
     * Garante que detalhes do grupo so sejam retornados para participantes.
     */
    @EntityGraph(attributePaths = {"dono", "membros"})
    Optional<Group> findByIdAndMembros_Id(Long groupId, Long userId);

}
