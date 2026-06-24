package com.busline.tranmaunhan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bookings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "UserId")
    private Users user;

    @Column(name = "BookingTime")
    private OffsetDateTime bookingTime;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "TotalAmount")
    private BigDecimal totalAmount;

    @Column(name = "BookingCode")
    private String bookingCode;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tickets> tickets = new ArrayList<>();

    public void addTicket(Tickets ticket) {
        tickets.add(ticket);
        ticket.setBooking(this);
    }
}
