package com.busline.tranmaunhan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "RoleName")
    private String roleName;

    @Column(name = "Description")
    private String description;

    @OneToMany(mappedBy = "role")
    private List<UserRoles> userRoles;
}
