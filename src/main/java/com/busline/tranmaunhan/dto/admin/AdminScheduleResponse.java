package com.busline.tranmaunhan.dto.admin;

import java.util.List;

public record AdminScheduleResponse(
        String selectedDate,
        String heading,
        String summary,
        List<DayOption> days,
        List<LocationOption> locations,
        List<TimeSlotColumn> columns
) {
    public record DayOption(
            String id,
            String label,
            String dateText
    ) {
    }

    public record LocationOption(
            Integer id,
            String name
    ) {
    }

    public record TimeSlotColumn(
            String slot,
            String subtitle,
            List<TripItem> trips
    ) {
    }

    public record TripItem(
            Integer tripId,
            String time,
            Integer originId,
            String origin,
            Integer destinationId,
            String destination,
            int emptySeats,
            String plate
    ) {
    }
}
