package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<Users> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);
}
