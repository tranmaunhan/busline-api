package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.auth.MessageResponse;
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
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Dat ve va quan ly booking")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(
            summary = "Dat ve (lock ghe cho thanh toan)",
            description = """
                    Tao booking moi cho nguoi dung da dang nhap.
                    He thong se:
                    - Kiem tra ghe con trong (AVAILABLE)
                    - Lock ghe (chuyen sang LOCKED) bang SELECT FOR UPDATE
                    - Tao Booking voi trang thai PENDING
                    - Tao Ticket cho tung ghe da chon
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "201", description = "Dat ve thanh cong, ghe da duoc lock")
    @ApiResponse(responseCode = "400", description = "Du lieu khong hop le hoac ghe khong con trong",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "401", description = "Chua dang nhap",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "404", description = "Khong tim thay chuyen xe / ghe / diem don tra",
            content = @Content(schema = @Schema()))
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        BookingResponse response = bookingService.createBooking(request, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{bookingId}/confirm-success")
    @Operation(
            summary = "Xac nhan dat ve thanh cong",
            description = """
                    Cap nhat booking tu trang thai cho xac nhan sang thanh cong.
                    Sau khi xac nhan, he thong se gui them thong bao dat ve thanh cong qua email.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Xac nhan dat ve thanh cong")
    @ApiResponse(responseCode = "400", description = "Booking khong o trang thai hop le",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "401", description = "Chua dang nhap",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "404", description = "Khong tim thay booking cua nguoi dung",
            content = @Content(schema = @Schema()))
    public ResponseEntity<BookingResponse> confirmBookingSuccess(
            @PathVariable @Positive Integer bookingId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        BookingResponse response = bookingService.confirmBookingSuccess(bookingId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{bookingId}")
    @Operation(
            summary = "Huy booking chua thanh toan",
            description = """
                    Cho phep nguoi dung huy booking cua chinh minh neu booking van dang o trang thai cho thanh toan.
                    Khi huy, he thong se xoa ticket, tra ghe ve AVAILABLE va xoa booking.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Huy booking thanh cong")
    @ApiResponse(responseCode = "400", description = "Booking da thanh toan hoac khong o trang thai co the huy",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "401", description = "Chua dang nhap",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "404", description = "Khong tim thay booking cua nguoi dung",
            content = @Content(schema = @Schema()))
    public ResponseEntity<MessageResponse> cancelPendingBooking(
            @PathVariable @Positive Integer bookingId,
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.cancelPendingBooking(bookingId, currentUser.getId()));
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Tra cuu ve da dat",
            description = "Tra cuu thong tin booking bang ma dat ve va so dien thoai cua nguoi dung."
    )
    @ApiResponse(responseCode = "200", description = "Tra cuu booking thanh cong")
    @ApiResponse(responseCode = "400", description = "bookingCode hoac phone khong hop le",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "404", description = "Khong tim thay booking phu hop",
            content = @Content(schema = @Schema()))
    public ResponseEntity<BookingResponse> lookupBooking(
            @RequestParam String bookingCode,
            @RequestParam String phone
    ) {
        BookingResponse response = bookingService.getBookingByCodeAndPhone(bookingCode, phone);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
            summary = "Danh sach booking cua toi",
            description = "Lay lai tat ca booking user dang dang nhap da dat",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Lay danh sach booking thanh cong")
    @ApiResponse(responseCode = "401", description = "Chua dang nhap",
            content = @Content(schema = @Schema()))
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal CustomUserDetails currentUser
    ) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(currentUser.getId()));
    }
}
