package com.busline.tranmaunhan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "VehicleTypes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleTypes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "TypeName")
    private String typeName;

    @Column(name = "TotalSeats")
    private Integer totalSeats;

    @OneToMany(mappedBy = "vehicleType")
    private List<Vehicles> vehicles;

    @OneToMany(mappedBy = "vehicleType")
    private List<SeatTemplates> seatTemplates;
}
