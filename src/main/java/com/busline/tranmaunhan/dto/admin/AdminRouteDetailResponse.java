package com.busline.tranmaunhan.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminRouteDetailResponse(
        Integer routeId,
        String route,
        Double distanceKm,
        Integer estimatedDurationMinutes,
        boolean canMutate,
        List<StopItem> stops,
        List<SegmentPriceItem> segmentPrices
) {
    public record StopItem(
            Integer stopOrder,
            Integer locationId,
            String locationName,
            Double distanceFromStartKm,
            Integer estimatedTimeFromStartMinutes
    ) {
    }

    public record SegmentPriceItem(
            Integer pickupStopOrder,
            String pickupLocationName,
            Integer dropoffStopOrder,
            String dropoffLocationName,
            BigDecimal price
    ) {
    }
}
