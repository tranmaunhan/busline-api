package com.busline.tranmaunhan.dto.trip;

public record TripSeatMapItemResponse(
        Integer tripSeatId,
        Integer seatTemplateId,
        String seatCode,
        Integer rowIndex,
        Integer colIndex,
        String deck,
        String seatType,
        String status
) {
}
