package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.CreateBookingRequest;
import com.busline.tranmaunhan.dto.booking.TicketResponse;
import com.busline.tranmaunhan.entity.Bookings;
import com.busline.tranmaunhan.entity.RouteStops;
import com.busline.tranmaunhan.entity.Tickets;
import com.busline.tranmaunhan.entity.Trips;
import com.busline.tranmaunhan.entity.TripSeats;
import com.busline.tranmaunhan.entity.Users;
import com.busline.tranmaunhan.repository.BookingRepository;
import com.busline.tranmaunhan.repository.RouteSegmentPriceRepository;
import com.busline.tranmaunhan.repository.RouteStopRepository;
import com.busline.tranmaunhan.repository.TicketRepository;
import com.busline.tranmaunhan.repository.TripRepository;
import com.busline.tranmaunhan.repository.TripSeatRepository;
import com.busline.tranmaunhan.repository.UsersRepository;
import com.busline.tranmaunhan.service.BookingNotificationService;
import com.busline.tranmaunhan.service.BookingResponseMapper;
import com.busline.tranmaunhan.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Integer BOOKING_STATUS_PENDING = 0;
    private static final Integer BOOKING_STATUS_CONFIRMED = 1;
    private static final Integer SEAT_STATUS_AVAILABLE = 0;
    private static final Integer SEAT_STATUS_LOCKED = 1;

    private final UsersRepository usersRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteSegmentPriceRepository routeSegmentPriceRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final BookingNotificationService bookingNotificationService;
    private final BookingResponseMapper bookingResponseMapper;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, Integer userId) {
        if (request.getPickupLocationId().equals(request.getDropoffLocationId())) {
            throw new IllegalArgumentException("Diem don va diem tra khong duoc giong nhau");
        }

        if (request.getTripSeatIds() == null || request.getTripSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Vui long chon it nhat mot ghe");
        }

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay thong tin nguoi dung"));

        Trips trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Khong tim thay chuyen xe voi id = " + request.getTripId()));

        List<Integer> sortedSeatIds = request.getTripSeatIds().stream()
                .distinct()
                .sorted()
                .toList();

        List<TripSeats> seats = tripSeatRepository.findByIdInWithLock(sortedSeatIds);

        if (seats.size() != sortedSeatIds.size()) {
            Set<Integer> foundIds = seats.stream()
                    .map(TripSeats::getId)
                    .collect(Collectors.toSet());

            List<Integer> missingIds = sortedSeatIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new NoSuchElementException("Khong tim thay ghe voi id: " + missingIds);
        }

        for (TripSeats seat : seats) {
            if (!seat.getTrip().getId().equals(trip.getId())) {
                throw new IllegalArgumentException(
                        "Ghe id=" + seat.getId() + " khong thuoc chuyen xe id=" + trip.getId());
            }

            if (!SEAT_STATUS_AVAILABLE.equals(seat.getStatus())) {
                throw new IllegalArgumentException(
                        "Ghe " + seat.getSeatTemplate().getSeatCode()
                                + " da duoc dat hoac dang bi khoa. Vui long chon ghe khac.");
            }
        }

        Integer routeId = trip.getRoute().getId();

        RouteStops pickupStop = routeStopRepository
                .findByRouteIdAndLocationId(routeId, request.getPickupLocationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Diem don (locationId=" + request.getPickupLocationId()
                                + ") khong thuoc tuyen xe nay"));

        RouteStops dropoffStop = routeStopRepository
                .findByRouteIdAndLocationId(routeId, request.getDropoffLocationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Diem tra (locationId=" + request.getDropoffLocationId()
                                + ") khong thuoc tuyen xe nay"));

        if (pickupStop.getStopOrder() >= dropoffStop.getStopOrder()) {
            throw new IllegalArgumentException("Diem don phai o truoc diem tra tren tuyen duong");
        }

        BigDecimal pricePerSeat = routeSegmentPriceRepository
                .findPriceByRouteAndLocations(
                        routeId,
                        request.getPickupLocationId(),
                        request.getDropoffLocationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Khong co gia ve cho hanh trinh da chon tren tuyen id=" + routeId));

        BigDecimal totalAmount = pricePerSeat.multiply(BigDecimal.valueOf(seats.size()));

        Bookings booking = new Bookings();
        booking.setUser(user);
        booking.setBookingTime(OffsetDateTime.now());
        booking.setStatus(BOOKING_STATUS_PENDING);
        booking.setTotalAmount(totalAmount);

        Bookings savedBooking = bookingRepository.saveAndFlush(booking);

        String bookingCode = generateBookingCode(savedBooking.getId());
        savedBooking.setBookingCode(bookingCode);
        savedBooking = bookingRepository.saveAndFlush(savedBooking);

        List<Tickets> tickets = new ArrayList<>();
        for (TripSeats seat : seats) {
            Tickets ticket = new Tickets();
            ticket.setBooking(savedBooking);
            ticket.setTrip(trip);
            ticket.setTripSeat(seat);
            ticket.setPickupStop(pickupStop);
            ticket.setDropoffStop(dropoffStop);
            ticket.setPrice(pricePerSeat);
            tickets.add(ticket);

            seat.setStatus(SEAT_STATUS_LOCKED);
        }

        List<Tickets> savedTickets = ticketRepository.saveAll(tickets);
        tripSeatRepository.saveAll(seats);
        savedBooking.setTickets(savedTickets);

        BookingResponse response = bookingResponseMapper.toBookingResponse(savedBooking);
        bookingNotificationService.sendBookingPendingNotification(user, response);
        return response;
    }

    @Override
    @Transactional
    public BookingResponse confirmBookingSuccess(Integer bookingId, Integer userId) {
        Bookings booking = bookingRepository.findByIdAndUserIdWithDetails(bookingId, userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay booking cua nguoi dung"));

        if (BOOKING_STATUS_CONFIRMED.equals(booking.getStatus())) {
            return bookingResponseMapper.toBookingResponse(booking);
        }

        if (!BOOKING_STATUS_PENDING.equals(booking.getStatus())) {
            throw new IllegalArgumentException("Booking khong o trang thai cho xac nhan thanh cong");
        }

        booking.setStatus(BOOKING_STATUS_CONFIRMED);
        Bookings savedBooking = bookingRepository.saveAndFlush(booking);

        BookingResponse response = bookingResponseMapper.toBookingResponse(savedBooking);
        bookingNotificationService.sendBookingConfirmedNotification(savedBooking.getUser(), response);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponse getBookingByCodeAndPhone(String bookingCode, String phone) {
        String normalizedBookingCode = normalizeRequiredField(bookingCode, "bookingCode");
        String normalizedPhone = normalizeRequiredField(phone, "phone");

        Bookings booking = bookingRepository.findByBookingCodeAndUserPhoneWithDetails(
                        normalizedBookingCode,
                        normalizedPhone)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay booking voi thong tin da cung cap"));

        return bookingResponseMapper.toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Integer userId) {
        if (!usersRepository.existsById(userId)) {
            throw new NoSuchElementException("Khong tim thay thong tin nguoi dung");
        }

        return bookingRepository.findByUserIdWithDetails(userId).stream()
                .map(bookingResponseMapper::toBookingResponse)
                .toList();
    }

    private String normalizeRequiredField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " khong duoc de trong");
        }
        return value.trim();
    }

    private String generateBookingCode(Integer bookingId) {
        return "SAIGONSTBK" + Integer.toString(bookingId, 36).toUpperCase();
    }
}
