package com.busline.tranmaunhan.dto.admin;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AdminUpsertVehicleRequest(
        @NotBlank(message = "licensePlate khong duoc de trong")
        @Size(max = 30, message = "licensePlate toi da 30 ky tu")
        String licensePlate,

        @Size(max = 120, message = "brand toi da 120 ky tu")
        String brand,

        @Min(value = 1950, message = "manufactureYear khong hop le")
        @Max(value = 2100, message = "manufactureYear khong hop le")
        Integer manufactureYear,

        @NotNull(message = "vehicleTypeId khong duoc de trong")
        @Positive(message = "vehicleTypeId phai > 0")
        Integer vehicleTypeId,

        @NotBlank(message = "status khong duoc de trong")
        String status
) {
}
