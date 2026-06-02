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

import java.math.BigDecimal;

@Entity
@Table(name = "RouteSegmentPrices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RouteSegmentPrices {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "RouteId")
    private Routes route;

    @ManyToOne
    @JoinColumn(name = "PickupStopId")
    private RouteStops pickupStop;

    @ManyToOne
    @JoinColumn(name = "DropoffStopId")
    private RouteStops dropoffStop;

    @Column(name = "Price")
    private BigDecimal price;
}
