package com.busline.tranmaunhan.dto.trip;

import java.math.BigDecimal;

public record PopularRouteResponse(
        Integer routeId,
        String originName,
        String destinationName,
        Integer estimatedDurationMinutes,
        BigDecimal startingPrice,
        long dailyTripCount
) {
}
