package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolesRepository extends JpaRepository<Roles, Integer> {

    Optional<Roles> findByRoleNameIgnoreCase(String roleName);
}
