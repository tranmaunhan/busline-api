package com.busline.tranmaunhan.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
public class AdminUpdateBookingRequest {

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
