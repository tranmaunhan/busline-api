package com.busline.tranmaunhan.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "UserRoles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRoles {

    @EmbeddedId
    private UserRolesId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "UserId")
    private Users user;

    @ManyToOne
    @MapsId("roleId")
    @JoinColumn(name = "RoleId")
    private Roles role;
}
