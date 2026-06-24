package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.CreateBookingRequest;

import java.util.List;

public interface BookingService {

    /**
     * Tạo booking mới: lock ghế đã chọn, tạo Booking + Tickets.
     *
     * @param request    thông tin đặt vé từ client
     * @param userId     id của user đang đăng nhập (lấy từ JWT)
     * @return BookingResponse chứa chi tiết booking vừa tạo
     */
    BookingResponse createBooking(CreateBookingRequest request, Integer userId);

    BookingResponse confirmBookingSuccess(Integer bookingId, Integer userId);

    /**
     * Tra cuu booking theo ma dat ve va so dien thoai cua nguoi dung.
     *
     * @param bookingCode ma dat ve
     * @param phone so dien thoai da dung khi dang ky/dat ve
     * @return BookingResponse chua thong tin ve da dat
     */
    BookingResponse getBookingByCodeAndPhone(String bookingCode, String phone);

    List<BookingResponse> getBookingsByUserId(Integer userId);
}
