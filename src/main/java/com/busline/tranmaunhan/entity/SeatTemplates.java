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
@Table(name = "SeatTemplates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatTemplates {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "VehicleTypeId")
    private VehicleTypes vehicleType;

    @Column(name = "SeatCode")
    private String seatCode;

    @Column(name = "RowIndex")
    private Integer rowIndex;

    @Column(name = "ColIndex")
    private Integer colIndex;

    @Column(name = "Deck")
    private String deck;

    @Column(name = "SeatType")
    private String seatType;

    @OneToMany(mappedBy = "seatTemplate")
    private List<TripSeats> tripSeats;
}
