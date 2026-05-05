package com.example.springApp.repository;

import com.example.springApp.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByCodigoUnico(String codigoUnico);
    boolean existsByDonoId(Long donoId);
    List<Group> findByMembros_Id(Long usuarioId);

}
