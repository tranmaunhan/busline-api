package com.busline.tranmaunhan.dto.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "tripId không được để trống")
    @Positive(message = "tripId phải là số dương")
    private Integer tripId;

    @NotEmpty(message = "Phải chọn ít nhất 1 ghế")
    @Size(min = 1, max = 10, message = "Số ghế chọn phải từ 1 đến 10")
    private List<@NotNull @Positive Integer> tripSeatIds;

    @NotNull(message = "pickupLocationId không được để trống")
    @Positive(message = "pickupLocationId phải là số dương")
    private Integer pickupLocationId;

    @NotNull(message = "dropoffLocationId không được để trống")
    @Positive(message = "dropoffLocationId phải là số dương")
    private Integer dropoffLocationId;
}
