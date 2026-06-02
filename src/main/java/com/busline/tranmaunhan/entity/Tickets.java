package com.busline.tranmaunhan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tickets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "BookingId")
    private Bookings booking;

    @ManyToOne
    @JoinColumn(name = "TripId")
    private Trips trip;

    @ManyToOne
    @JoinColumn(name = "TripSeatId")
    private TripSeats tripSeat;

    @ManyToOne
    @JoinColumn(name = "PickupStopId")
    private RouteStops pickupStop;

    @ManyToOne
    @JoinColumn(name = "DropoffStopId")
    private RouteStops dropoffStop;

    @Column(name = "Price")
    private BigDecimal price;
}
