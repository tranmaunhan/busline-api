package com.busline.tranmaunhan.dto.admin;

import java.util.List;

public record AdminFleetResponse(
        List<SummaryItem> summary,
        List<VehicleItem> vehicles,
        List<VehicleTypeOption> vehicleTypes,
        List<StatusOption> statusOptions
) {
    public record SummaryItem(
            String label,
            String value,
            String note
    ) {
    }

    public record VehicleItem(
            Integer vehicleId,
            String code,
            String type,
            Integer vehicleTypeId,
            String status,
            String rawStatus,
            String activityLabel,
            String activityValue,
            String brand,
            Integer manufactureYear,
            Integer totalSeats
    ) {
    }

    public record VehicleTypeOption(
            Integer id,
            String name,
            Integer totalSeats
    ) {
    }

    public record StatusOption(
            String value,
            String label
    ) {
    }
}
