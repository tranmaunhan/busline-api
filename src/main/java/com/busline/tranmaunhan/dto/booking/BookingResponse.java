package com.busline.tranmaunhan.dto.booking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private Integer bookingId;
    private String bookingCode;

    private OffsetDateTime bookingTime;
    private Integer status;
    private BigDecimal totalAmount;

    private Integer tripId;
    private OffsetDateTime tripDepartureTime;

    private String routeOrigin;
    private String routeDestination;

    private String pickupLocationName;
    private String dropoffLocationName;

    private List<TicketResponse> tickets;

}
