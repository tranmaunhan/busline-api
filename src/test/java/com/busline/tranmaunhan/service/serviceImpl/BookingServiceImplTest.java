package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.dto.auth.MessageResponse;
import com.busline.tranmaunhan.entity.Bookings;
import com.busline.tranmaunhan.entity.SeatTemplates;
import com.busline.tranmaunhan.entity.Tickets;
import com.busline.tranmaunhan.entity.TripSeats;
import com.busline.tranmaunhan.repository.BookingRepository;
import com.busline.tranmaunhan.repository.RouteSegmentPriceRepository;
import com.busline.tranmaunhan.repository.RouteStopRepository;
import com.busline.tranmaunhan.repository.TicketRepository;
import com.busline.tranmaunhan.repository.TripRepository;
import com.busline.tranmaunhan.repository.TripSeatRepository;
import com.busline.tranmaunhan.repository.UsersRepository;
import com.busline.tranmaunhan.service.BookingNotificationService;
import com.busline.tranmaunhan.service.BookingResponseMapper;
import com.busline.tranmaunhan.service.ExpiredBookingCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceImplTest {

    private BookingRepository bookingRepository;
    private TripSeatRepository tripSeatRepository;
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        tripSeatRepository = mock(TripSeatRepository.class);

        bookingService = new BookingServiceImpl(
                mock(UsersRepository.class),
                mock(TripRepository.class),
                tripSeatRepository,
                mock(RouteStopRepository.class),
                mock(RouteSegmentPriceRepository.class),
                bookingRepository,
                mock(TicketRepository.class),
                mock(ExpiredBookingCleanupService.class),
                mock(BookingNotificationService.class),
                mock(BookingResponseMapper.class)
        );
    }

    @Test
    void shouldCancelPendingBookingAndReleaseSeats() {
        TripSeats seat = new TripSeats();
        seat.setId(10);
        seat.setStatus(1);
        SeatTemplates seatTemplate = new SeatTemplates();
        seatTemplate.setSeatCode("A01");
        seat.setSeatTemplate(seatTemplate);

        Tickets ticket = new Tickets();
        ticket.setTripSeat(seat);

        Bookings booking = new Bookings();
        booking.setId(1);
        booking.setStatus(0);
        booking.setTickets(List.of(ticket));

        when(bookingRepository.findByIdAndUserIdWithDetails(1, 99))
                .thenReturn(Optional.of(booking));

        MessageResponse response = bookingService.cancelPendingBooking(1, 99);

        assertEquals("Huy booking thanh cong", response.message());
        assertEquals(0, seat.getStatus());
        verify(tripSeatRepository).saveAll(List.of(seat));
        verify(bookingRepository).delete(booking);
    }

    @Test
    void shouldRejectConfirmedBookingCancellation() {
        Bookings booking = new Bookings();
        booking.setStatus(1);
        booking.setTickets(List.of(new Tickets()));

        when(bookingRepository.findByIdAndUserIdWithDetails(1, 99))
                .thenReturn(Optional.of(booking));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookingService.cancelPendingBooking(1, 99)
        );

        assertEquals("Booking da thanh toan, khong the huy", exception.getMessage());
        verify(bookingRepository, never()).delete(any());
    }

    @Test
    void shouldThrowWhenBookingNotFound() {
        when(bookingRepository.findByIdAndUserIdWithDetails(1, 99))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> bookingService.cancelPendingBooking(1, 99));
    }
}
