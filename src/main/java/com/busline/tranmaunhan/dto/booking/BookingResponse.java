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
    private LocalDateTime bookingTime;
    private Integer status;
    private BigDecimal totalAmount;

    // Trip info
    private Integer tripId;
    private LocalDateTime tripDepartureTime;

    // Route info
    private String routeOrigin;
    private String routeDestination;

    // Pickup / Dropoff
    private String pickupLocationName;
    private String dropoffLocationName;

    private List<TicketResponse> tickets;
}
