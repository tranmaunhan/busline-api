package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.Users;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Integer> {

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    Optional<Users> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"userRoles", "userRoles.role"})
    List<Users> findAllByOrderByCreatedAtDesc();
}
