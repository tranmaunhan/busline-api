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
import com.busline.tranmaunhan.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Integer SEAT_STATUS_AVAILABLE = 0;
    private static final Integer SEAT_STATUS_LOCKED = 1;

    private final UsersRepository usersRepository;
    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteSegmentPriceRepository routeSegmentPriceRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

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
        booking.setStatus(0);
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

        return toBookingResponse(savedBooking);
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

        return toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> getBookingsByUserId(Integer userId) {
        if (!usersRepository.existsById(userId)) {
            throw new NoSuchElementException("Khong tim thay thong tin nguoi dung");
        }

        return bookingRepository.findByUserIdWithDetails(userId).stream()
                .map(this::toBookingResponse)
                .toList();
    }

    private BookingResponse toBookingResponse(Bookings booking) {
        if (booking.getTickets() == null || booking.getTickets().isEmpty()) {
            throw new IllegalStateException("Booking khong co ticket de tra cuu");
        }

        List<Tickets> sortedTickets = booking.getTickets().stream()
                .sorted(Comparator.comparing(Tickets::getId))
                .toList();

        Tickets firstTicket = sortedTickets.get(0);
        Trips trip = firstTicket.getTrip();
        RouteStops pickupStop = firstTicket.getPickupStop();
        RouteStops dropoffStop = firstTicket.getDropoffStop();

        List<TicketResponse> ticketResponses = sortedTickets.stream()
                .map(ticket -> new TicketResponse(
                        ticket.getId(),
                        ticket.getTripSeat().getId(),
                        ticket.getTripSeat().getSeatTemplate().getSeatCode(),
                        ticket.getTripSeat().getSeatTemplate().getDeck(),
                        ticket.getTripSeat().getSeatTemplate().getSeatType(),
                        ticket.getPrice()))
                .toList();

        return new BookingResponse(
                booking.getId(),
                booking.getBookingCode(),
                booking.getBookingTime(),
                booking.getStatus(),
                booking.getTotalAmount(),
                trip.getId(),
                trip.getDepartureTime(),
                trip.getRoute().getOrigin().getName(),
                trip.getRoute().getDestination().getName(),
                pickupStop.getLocation().getName(),
                dropoffStop.getLocation().getName(),
                ticketResponses);
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
