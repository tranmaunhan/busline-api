package com.busline.tranmaunhan.dto.trip;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TripDetailSeatLayoutItemResponse(
        @JsonProperty("trip_seat_id")
        Integer tripSeatId,
        @JsonProperty("seat_status")
        Integer seatStatus,
        @JsonProperty("seat_code")
        String seatCode,
        @JsonProperty("row_index")
        Integer rowIndex,
        @JsonProperty("col_index")
        Integer colIndex,
        @JsonProperty("deck")
        String deck,
        @JsonProperty("seat_type")
        String seatType
) {
}
