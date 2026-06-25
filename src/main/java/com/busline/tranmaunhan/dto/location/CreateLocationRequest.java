package com.busline.tranmaunhan.dto.location;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLocationRequest(
        @NotBlank(message = "Ten dia diem khong duoc de trong")
        @Size(max = 255, message = "Ten dia diem khong duoc vuot qua 255 ky tu")
        String name,

        @NotBlank(message = "Dia chi khong duoc de trong")
        @Size(max = 500, message = "Dia chi khong duoc vuot qua 500 ky tu")
        String address,

        @NotBlank(message = "Loai dia diem khong duoc de trong")
        @Size(max = 100, message = "Loai dia diem khong duoc vuot qua 100 ky tu")
        String type
) {
}
