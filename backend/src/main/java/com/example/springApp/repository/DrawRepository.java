package com.example.springApp.repository;

import com.example.springApp.model.Draw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DrawRepository extends JpaRepository<Draw, Long> {

    List<Draw> findByGrupoId(Long grupoId);

    // Para o usuario ver quem ele tirou em um grupo especifico
    Optional<Draw> findByGroupIdAndRemetenteId(Long groupId, Long giverId);

}