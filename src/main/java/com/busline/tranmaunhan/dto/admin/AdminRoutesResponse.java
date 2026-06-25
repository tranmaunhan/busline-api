package com.busline.tranmaunhan.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminRoutesResponse(
        List<RouteCatalogItem> routes,
        List<RoutePriorityItem> highlights
) {
    public record RouteCatalogItem(
            Integer routeId,
            String route,
            double distanceKm,
            Integer estimatedDurationMinutes,
            double averageTripsPerDay,
            BigDecimal averageRevenuePerDay,
            double occupancyRate
    ) {
    }

    public record RoutePriorityItem(
            Integer routeId,
            String name,
            long tickets,
            BigDecimal revenue,
            double occupancyRate
    ) {
    }
}
