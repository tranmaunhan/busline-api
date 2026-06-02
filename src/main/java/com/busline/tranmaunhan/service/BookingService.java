package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.CreateBookingRequest;

public interface BookingService {

    /**
     * Tạo booking mới: lock ghế đã chọn, tạo Booking + Tickets.
     *
     * @param request    thông tin đặt vé từ client
     * @param userId     id của user đang đăng nhập (lấy từ JWT)
     * @return BookingResponse chứa chi tiết booking vừa tạo
     */
    BookingResponse createBooking(CreateBookingRequest request, Integer userId);
}
