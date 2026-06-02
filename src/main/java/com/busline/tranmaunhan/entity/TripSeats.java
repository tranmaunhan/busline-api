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
@Table(name = "TripSeats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TripSeats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "TripId")
    private Trips trip;

    @ManyToOne
    @JoinColumn(name = "SeatTemplateId")
    private SeatTemplates seatTemplate;

    @Column(name = "Status")
    private Integer status;

    @OneToMany(mappedBy = "tripSeat")
    private List<Tickets> tickets;
}
