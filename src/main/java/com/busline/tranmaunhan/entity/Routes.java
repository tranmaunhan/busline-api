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
@Table(name = "Routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Routes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "OriginId")
    private Locations origin;

    @ManyToOne
    @JoinColumn(name = "DestinationId")
    private Locations destination;

    @Column(name = "DistanceKm")
    private Double distanceKm;

    @Column(name = "EstimatedDurationMinutes")
    private Integer estimatedDurationMinutes;

    @OneToMany(mappedBy = "route")
    private List<RouteStops> routeStops;

    @OneToMany(mappedBy = "route")
    private List<Trips> trips;
}
