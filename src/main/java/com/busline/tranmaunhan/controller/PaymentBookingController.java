package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.payment.BookingPaymentStatusResponse;
import com.busline.tranmaunhan.service.BookingPaymentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping({"/api/bookings", "/bookings"})
@RequiredArgsConstructor
public class PaymentBookingController {

    private final BookingPaymentStatusService bookingPaymentStatusService;

    @GetMapping("/{bookingCode}/status")
    public ResponseEntity<BookingPaymentStatusResponse> getBookingStatus(@PathVariable String bookingCode) {
        if (!StringUtils.hasText(bookingCode)) {
            return ResponseEntity.badRequest()
                    .body(BookingPaymentStatusResponse.notFound(bookingCode));
        }

        Optional<Integer> status = bookingPaymentStatusService.getBookingStatusByCode(bookingCode);
        if (status.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(BookingPaymentStatusResponse.notFound(bookingCode));
        }

        return ResponseEntity.ok(BookingPaymentStatusResponse.found(bookingCode, status.get()));
    }
}
