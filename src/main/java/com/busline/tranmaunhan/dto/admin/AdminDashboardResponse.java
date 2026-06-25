package com.busline.tranmaunhan.dto.admin;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardResponse(
        String updatedAtLabel,
        List<OverviewStat> overviewStats,
        List<MetricCard> metricCards,
        List<RevenuePoint> revenueSeries,
        List<RouteHighlight> topRoutes,
        List<UpcomingTrip> upcomingTrips,
        List<RecentBooking> latestBookings,
        List<AlertItem> alerts
) {
    public record OverviewStat(
            String label,
            String value,
            String caption
    ) {
    }

    public record MetricCard(
            String label,
            String value,
            String detail,
            String tone
    ) {
    }

    public record RevenuePoint(
            String label,
            BigDecimal value
    ) {
    }

    public record RouteHighlight(
            String name,
            long tickets,
            BigDecimal revenue,
            double occupancyRate
    ) {
    }

    public record UpcomingTrip(
            Integer tripId,
            String code,
            String route,
            String departure,
            String gate,
            String seats,
            String driver,
            String status
    ) {
    }

    public record RecentBooking(
            Integer bookingId,
            String bookingCode,
            String customer,
            String route,
            String seats,
            BigDecimal amount,
            String payment,
            String time
    ) {
    }

    public record AlertItem(
            String level,
            String title,
            String detail
    ) {
    }
}
