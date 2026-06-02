package com.busline.tranmaunhan.repository;

import com.busline.tranmaunhan.entity.UserRoles;
import com.busline.tranmaunhan.entity.UserRolesId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRolesRepository extends JpaRepository<UserRoles, UserRolesId> {
}
