package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.entity.Users;

public interface BookingNotificationService {

    void sendBookingPendingNotification(Users user, BookingResponse bookingResponse);

    void sendBookingConfirmedNotification(Users user, BookingResponse bookingResponse);
}
