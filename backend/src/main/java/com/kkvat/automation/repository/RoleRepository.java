package com.kkvat.automation.repository;

import com.kkvat.automation.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    Boolean existsByName(String name);

    List<Role> findByIsActive(Boolean isActive);

    List<Role> findByNameContaining(String keyword);

    List<Role> findByCreatedBy(Long createdBy);
}
