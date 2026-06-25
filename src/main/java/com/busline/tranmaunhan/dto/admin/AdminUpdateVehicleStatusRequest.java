package com.busline.tranmaunhan.dto.admin;

import jakarta.validation.constraints.NotBlank;

public record AdminUpdateVehicleStatusRequest(
        @NotBlank(message = "status khong duoc de trong")
        String status
) {
}
