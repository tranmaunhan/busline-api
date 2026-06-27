package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.TicketResponse;
import com.busline.tranmaunhan.entity.Bookings;
import com.busline.tranmaunhan.entity.RouteStops;
import com.busline.tranmaunhan.entity.Tickets;
import com.busline.tranmaunhan.entity.Trips;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class BookingResponseMapper {

    public BookingResponse toBookingResponse(Bookings booking) {
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
                booking.getUser() == null ? null : booking.getUser().getId(),
                booking.getBookingCode(),
                booking.getBookingTime(),
                booking.getStatus(),
                booking.getTotalAmount(),
                booking.getContactName(),
                booking.getContactPhone(),
                booking.getContactEmail(),
                booking.getNote(),
                booking.getPaymentExpiry(),
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
