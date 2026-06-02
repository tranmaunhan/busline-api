package com.busline.tranmaunhan.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private LocalDateTime bookingTime;

    @Column(name = "Status")
    private Integer status;

    @Column(name = "TotalAmount")
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "booking")
    private List<Tickets> tickets;
}
