package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingPaymentStatusService {

    private final BookingRepository bookingRepository;

    public Optional<Integer> getBookingStatusByCode(String bookingCode) {
        if (!StringUtils.hasText(bookingCode)) {
            return Optional.empty();
        }

        return bookingRepository.findStatusByBookingCode(bookingCode.trim());
    }
}
