package com.busline.tranmaunhan.dto.booking;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateBookingRequest {

    @NotNull(message = "tripId khong duoc de trong")
    @Positive(message = "tripId phai la so duong")
    private Integer tripId;

    @NotEmpty(message = "Phai chon it nhat 1 ghe")
    @Size(min = 1, max = 10, message = "So ghe chon phai tu 1 den 10")
    private List<@NotNull @Positive Integer> tripSeatIds;

    @NotNull(message = "pickupLocationId khong duoc de trong")
    @Positive(message = "pickupLocationId phai la so duong")
    private Integer pickupLocationId;

    @NotNull(message = "dropoffLocationId khong duoc de trong")
    @Positive(message = "dropoffLocationId phai la so duong")
    private Integer dropoffLocationId;

    @NotBlank(message = "contactName khong duoc de trong")
    @Size(max = 100, message = "contactName khong duoc vuot qua 100 ky tu")
    private String contactName;

    @NotBlank(message = "contactPhone khong duoc de trong")
    @Size(max = 20, message = "contactPhone khong duoc vuot qua 20 ky tu")
    private String contactPhone;

    @NotBlank(message = "contactEmail khong duoc de trong")
    @Email(message = "contactEmail khong hop le")
    @Size(max = 255, message = "contactEmail khong duoc vuot qua 255 ky tu")
    private String contactEmail;

    private String note;

    @NotNull(message = "paymentExpiry khong duoc de trong")
    @Future(message = "paymentExpiry phai la thoi diem trong tuong lai")
    private OffsetDateTime paymentExpiry;
}
