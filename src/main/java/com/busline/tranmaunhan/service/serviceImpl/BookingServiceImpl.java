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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private static final Integer SEAT_STATUS_AVAILABLE = 0;
    private static final Integer SEAT_STATUS_LOCKED    = 1;
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

        // ── 1. Validate cơ bản ───────────────────────────────────────────────
        if (request.getPickupLocationId().equals(request.getDropoffLocationId())) {
            throw new IllegalArgumentException("Diem don va diem tra khong duoc giong nhau");
        }

        // ── 2. Kiểm tra user tồn tại ─────────────────────────────────────────
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay thong tin nguoi dung"));

        // ── 3. Kiểm tra chuyến xe hợp lệ và đang hoạt động ──────────────────
        Trips trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Khong tim thay chuyen xe voi id = " + request.getTripId()));

        // ── 4. Lock ghế bằng SELECT FOR UPDATE (tránh race condition) ─────────
        // Sắp xếp ids trước khi lock để tránh deadlock khi nhiều transaction cùng lock
        List<Integer> sortedSeatIds = request.getTripSeatIds().stream()
                .distinct()
                .sorted()
                .toList();

        List<TripSeats> seats = tripSeatRepository.findByIdInWithLock(sortedSeatIds);

        // Kiểm tra tất cả ghế yêu cầu đều tìm thấy
        if (seats.size() != sortedSeatIds.size()) {
            Set<Integer> foundIds = seats.stream()
                    .map(TripSeats::getId)
                    .collect(Collectors.toSet());
            List<Integer> missingIds = sortedSeatIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            throw new NoSuchElementException("Khong tim thay ghe voi id: " + missingIds);
        }

        // ── 5. Validate từng ghế ─────────────────────────────────────────────
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
            throw new IllegalArgumentException(
                    "Diem don phai o truoc diem tra tren tuyen duong");
        }

        BigDecimal pricePerSeat = routeSegmentPriceRepository
                .findPriceByRouteAndLocations(routeId,
                        request.getPickupLocationId(),
                        request.getDropoffLocationId())
                .orElseThrow(() -> new NoSuchElementException(
                        "Khong co gia ve cho hanh trinh da chon tren tuyen id=" + routeId));

        Bookings booking = new Bookings();
        booking.setUser(user);
        booking.setBookingTime(LocalDateTime.now());
        booking.setStatus(0);
        booking.setTotalAmount(pricePerSeat.multiply(BigDecimal.valueOf(seats.size())));
        Bookings savedBooking = bookingRepository.save(booking);

        // ── 9. Tạo Tickets & Lock ghế ─────────────────────────────────────────
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

            // Lock ghế — đổi trạng thái sang LOCKED
            seat.setStatus(SEAT_STATUS_LOCKED);
        }

        ticketRepository.saveAll(tickets);
        tripSeatRepository.saveAll(seats); // flush status LOCKED vào DB

        // ── 10. Trả về response ───────────────────────────────────────────────
        List<TicketResponse> ticketResponses = new ArrayList<>();
        for (int i = 0; i < tickets.size(); i++) {
            Tickets t = tickets.get(i);
            TripSeats s = seats.get(i);
            ticketResponses.add(new TicketResponse(
                    t.getId(),
                    s.getId(),
                    s.getSeatTemplate().getSeatCode(),
                    s.getSeatTemplate().getDeck(),
                    s.getSeatTemplate().getSeatType(),
                    t.getPrice()
            ));
        }

        return new BookingResponse(
                savedBooking.getId(),
                savedBooking.getBookingTime(),
                savedBooking.getStatus(),
                savedBooking.getTotalAmount(),
                trip.getId(),
                trip.getDepartureTime(),
                trip.getRoute().getOrigin().getName(),
                trip.getRoute().getDestination().getName(),
                pickupStop.getLocation().getName(),
                dropoffStop.getLocation().getName(),
                ticketResponses
        );
    }
}
