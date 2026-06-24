package com.busline.tranmaunhan.dto.payment;

public record BookingPaymentStatusResponse(
        boolean success,
        String message,
        String bookingCode,
        Integer status
) {

    public static BookingPaymentStatusResponse found(String bookingCode, Integer status) {
        return new BookingPaymentStatusResponse(
                true,
                "Booking status retrieved successfully",
                bookingCode,
                status
        );
    }

    public static BookingPaymentStatusResponse notFound(String bookingCode) {
        return new BookingPaymentStatusResponse(
                false,
                "Booking not found",
                bookingCode,
                null
        );
    }
}
