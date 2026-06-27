package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.entity.Bookings;

public interface BookingNotificationService {

    void sendBookingPendingNotification(Bookings booking, BookingResponse bookingResponse);

    void sendBookingConfirmedNotification(Bookings booking, BookingResponse bookingResponse);
}
