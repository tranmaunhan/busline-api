package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.CreateBookingRequest;
import com.busline.tranmaunhan.security.CustomUserDetails;
import com.busline.tranmaunhan.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Đặt vé và quản lý booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
            summary = "Đặt vé (lock ghế chờ thanh toán)",
            description = """
                    Tạo booking mới cho người dùng đã đăng nhập.
                    Hệ thống sẽ:
                    - Kiểm tra ghế còn trống (AVAILABLE)
                    - Lock ghế (chuyển sang LOCKED) bằng SELECT FOR UPDATE
                    - Tạo Booking với trạng thái PENDING
                    - Tạo Ticket cho từng ghế đã chọn
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Đặt vé thành công, ghế đã được lock")
    @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc ghế không còn trống",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "401", description = "Chưa đăng nhập",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến xe / ghế / điểm đón trả",
            content = @Content(schema = @Schema()))
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        BookingResponse response = bookingService.createBooking(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
