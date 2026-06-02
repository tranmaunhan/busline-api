package com.busline.tranmaunhan.dto.location;

public record LocationResponse(
        Integer id,
        String name,
        String address,
        String type
) {
}
