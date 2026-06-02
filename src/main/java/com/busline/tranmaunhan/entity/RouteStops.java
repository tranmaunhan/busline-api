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
@Table(name = "RouteStops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteStops {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "RouteId")
    private Routes route;

    @ManyToOne
    @JoinColumn(name = "LocationId")
    private Locations location;

    @Column(name = "StopOrder")
    private Integer stopOrder;

    @Column(name = "DistanceFromStartKm")
    private Double distanceFromStartKm;

    @Column(name = "EstimatedTimeFromStartMinutes")
    private Integer estimatedTimeFromStartMinutes;

    @OneToMany(mappedBy = "pickupStop")
    private List<Tickets> pickupTickets;

    @OneToMany(mappedBy = "dropoffStop")
    private List<Tickets> dropoffTickets;
}
