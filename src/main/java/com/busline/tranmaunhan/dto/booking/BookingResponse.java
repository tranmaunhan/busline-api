package com.busline.tranmaunhan.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private Integer bookingId;
    private String bookingCode;

    private LocalDateTime bookingTime;
    private Integer status;
    private BigDecimal totalAmount;

    private Integer tripId;
    private LocalDateTime tripDepartureTime;

    private String routeOrigin;
    private String routeDestination;

    private String pickupLocationName;
    private String dropoffLocationName;

    private List<TicketResponse> tickets;
}