package com.busline.tranmaunhan.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "TripSchedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripSchedules {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "RouteId")
    private Routes route;

    @ManyToOne
    @JoinColumn(name = "VehicleId")
    private Vehicles vehicle;

    @Column(name = "DepartureTime")
    private LocalTime departureTime;

    @Column(name = "StartDate")
    private LocalDate startDate;

    @Column(name = "EndDate")
    private LocalDate endDate;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "CreatedAt")
    private OffsetDateTime createdAt;

    @Column(name = "UpdatedAt")
    private OffsetDateTime updatedAt;
}
