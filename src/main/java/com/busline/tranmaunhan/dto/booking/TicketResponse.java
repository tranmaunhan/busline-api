package com.busline.tranmaunhan.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class TicketResponse {

    private Integer ticketId;
    private Integer tripSeatId;
    private String seatCode;
    private String deck;
    private String seatType;
    private BigDecimal price;
}
