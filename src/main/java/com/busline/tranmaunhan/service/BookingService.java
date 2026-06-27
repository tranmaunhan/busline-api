package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.admin.AdminUpdateBookingRequest;
import com.busline.tranmaunhan.dto.auth.MessageResponse;
import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.CreateBookingRequest;

import java.util.List;

public interface BookingService {

    /**
     * Tao booking moi: lock ghe da chon, tao Booking + Tickets.
     *
     * @param request thong tin dat ve tu client
     * @param userId id cua user dang dang nhap, co the null voi khach vang lai
     * @return BookingResponse chua chi tiet booking vua tao
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

    MessageResponse cancelPendingBooking(Integer bookingId, Integer userId);

    BookingResponse getBookingByIdForAdmin(Integer bookingId);

    BookingResponse updatePendingBookingByAdmin(Integer bookingId, AdminUpdateBookingRequest request);

    MessageResponse cancelPendingBookingByAdmin(Integer bookingId);
}
