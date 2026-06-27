package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.entity.Bookings;
import com.busline.tranmaunhan.entity.Tickets;
import com.busline.tranmaunhan.entity.TripSeats;
import com.busline.tranmaunhan.repository.BookingRepository;
import com.busline.tranmaunhan.repository.TripSeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExpiredBookingCleanupService {

    private static final Integer BOOKING_STATUS_PENDING = 0;
    private static final Integer SEAT_STATUS_AVAILABLE = 0;
    private static final Integer SEAT_STATUS_LOCKED = 1;

    private final BookingRepository bookingRepository;
    private final TripSeatRepository tripSeatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int cleanupExpiredPendingBookings() {
        List<Bookings> expiredBookings = bookingRepository.findExpiredPendingBookingsWithDetails(
                BOOKING_STATUS_PENDING,
                OffsetDateTime.now());

        if (expiredBookings.isEmpty()) {
            return 0;
        }

        Set<TripSeats> seatsToRelease = new LinkedHashSet<>();
        for (Bookings booking : expiredBookings) {
            if (booking.getTickets() == null || booking.getTickets().isEmpty()) {
                continue;
            }

            for (Tickets ticket : booking.getTickets()) {
                TripSeats tripSeat = ticket.getTripSeat();
                if (tripSeat != null && SEAT_STATUS_LOCKED.equals(tripSeat.getStatus())) {
                    tripSeat.setStatus(SEAT_STATUS_AVAILABLE);
                    seatsToRelease.add(tripSeat);
                }
            }
        }

        if (!seatsToRelease.isEmpty()) {
            tripSeatRepository.saveAll(seatsToRelease);
        }

        bookingRepository.deleteAll(expiredBookings);
        log.info("Deleted {} expired pending bookings and released {} seats", expiredBookings.size(), seatsToRelease.size());
        return expiredBookings.size();
    }
}
