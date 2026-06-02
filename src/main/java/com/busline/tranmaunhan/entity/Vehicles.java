package com.busline.tranmaunhan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "Vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "LicensePlate")
    private String licensePlate;

    @Column(name = "Brand")
    private String brand;

    @Column(name = "ManufactureYear")
    private Integer manufactureYear;

    @ManyToOne
    @JoinColumn(name = "VehicleTypeId")
    private VehicleTypes vehicleType;

    @Column(name = "Status")
    private String status;

    @OneToMany(mappedBy = "vehicle")
    private List<Trips> trips;
}
