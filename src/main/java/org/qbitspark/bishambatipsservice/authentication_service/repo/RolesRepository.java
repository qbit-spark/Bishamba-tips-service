package org.qbitspark.bishambatipsservice.authentication_service.repo;


import org.qbitspark.bishambatipsservice.authentication_service.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
public interface RolesRepository extends JpaRepository<Roles, UUID> {
    Optional<Roles> findByRoleName(String name);
}
