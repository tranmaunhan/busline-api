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

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Trips {

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
    private LocalDateTime departureTime;

    @Column(name = "Status")
    private String status;

    @OneToMany(mappedBy = "trip")
    private List<TripSeats> tripSeats;

    @OneToMany(mappedBy = "trip")
    private List<Tickets> tickets;
}
