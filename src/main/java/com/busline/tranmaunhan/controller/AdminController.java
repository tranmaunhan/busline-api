package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.admin.AdminCreateRouteRequest;
import com.busline.tranmaunhan.dto.admin.AdminCreateTripScheduleRequest;
import com.busline.tranmaunhan.dto.admin.AdminDashboardResponse;
import com.busline.tranmaunhan.dto.admin.AdminFleetResponse;
import com.busline.tranmaunhan.dto.admin.AdminGenerateTripsRequest;
import com.busline.tranmaunhan.dto.admin.AdminGeneratedTripsResponse;
import com.busline.tranmaunhan.dto.admin.AdminRouteDetailResponse;
import com.busline.tranmaunhan.dto.admin.AdminRoutesResponse;
import com.busline.tranmaunhan.dto.admin.AdminScheduleResponse;
import com.busline.tranmaunhan.dto.admin.AdminStaffResponse;
import com.busline.tranmaunhan.dto.admin.AdminTripBookingSeatMapResponse;
import com.busline.tranmaunhan.dto.admin.AdminTripScheduleResponse;
import com.busline.tranmaunhan.dto.admin.AdminUpdateBookingRequest;
import com.busline.tranmaunhan.dto.admin.AdminUpdateVehicleStatusRequest;
import com.busline.tranmaunhan.dto.admin.AdminUpsertVehicleRequest;
import com.busline.tranmaunhan.dto.auth.MessageResponse;
import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.booking.CreateBookingRequest;
import com.busline.tranmaunhan.dto.location.CreateLocationRequest;
import com.busline.tranmaunhan.dto.location.LocationResponse;
import com.busline.tranmaunhan.service.LocationService;
import com.busline.tranmaunhan.service.AdminService;
import com.busline.tranmaunhan.service.BookingService;
import com.busline.tranmaunhan.service.ExpiredBookingCleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Thong tin tong hop cho trang quan tri nha xe")
public class AdminController {

    private final AdminService adminService;
    private final LocationService locationService;
    private final BookingService bookingService;
    private final ExpiredBookingCleanupService expiredBookingCleanupService;

    @GetMapping("/dashboard")
    @Operation(summary = "Lay dashboard admin", description = "Tong hop doanh thu, chuyen xe, booking va canh bao van hanh")
    @ApiResponse(responseCode = "200", description = "Lay dashboard thanh cong")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboard());
    }

    @DeleteMapping("/bookings/expired")
    @Operation(summary = "Xoa booking het han thanh toan", description = "Xoa cac booking pending da qua han thanh toan va mo lai ghe")
    @ApiResponse(responseCode = "200", description = "Xoa booking het han thanh cong")
    public ResponseEntity<MessageResponse> deleteExpiredBookings() {
        int deletedCount = expiredBookingCleanupService.cleanupExpiredPendingBookings();
        return ResponseEntity.ok(new MessageResponse("Da xoa " + deletedCount + " booking het han thanh toan"));
    }

    @GetMapping("/schedule")
    @Operation(summary = "Lay lich chay admin", description = "Lay lich chay theo ngay va bo loc diem di diem den")
    @ApiResponse(responseCode = "200", description = "Lay lich chay thanh cong")
    public ResponseEntity<AdminScheduleResponse> getSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @Positive Integer originId,
            @RequestParam(required = false) @Positive Integer destinationId
    ) {
        return ResponseEntity.ok(adminService.getSchedule(date, originId, destinationId));
    }

    @GetMapping("/trips/{tripId}/booking-seat-map")
    @Operation(summary = "Lay so do ghe admin kem thong tin dat cho", description = "Tra ve danh sach ghe, thong tin khach da dat va trang thai thanh toan cho tung ghe")
    @ApiResponse(responseCode = "200", description = "Lay so do ghe admin thanh cong")
    public ResponseEntity<AdminTripBookingSeatMapResponse> getTripBookingSeatMap(
            @PathVariable @Positive Integer tripId,
            @RequestParam(required = false) @Positive Integer pickupLocationId,
            @RequestParam(required = false) @Positive Integer dropoffLocationId
    ) {
        return ResponseEntity.ok(adminService.getTripBookingSeatMap(tripId, pickupLocationId, dropoffLocationId));
    }

    @PostMapping("/bookings/guest")
    @Operation(summary = "Nhan vien tao booking cho khach vang lai", description = "Tao booking guest tu man hinh admin, khong gan booking vao tai khoan nhan vien dang nhap")
    @ApiResponse(responseCode = "201", description = "Tao booking guest thanh cong")
    public ResponseEntity<BookingResponse> createGuestBooking(
            @Valid @RequestBody CreateBookingRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request, null));
    }

    @GetMapping("/bookings/{bookingId}")
    @Operation(summary = "Lay chi tiet booking cho admin", description = "Lay day du thong tin booking de nhan vien doi soat, sua lien he hoac huy don cho thanh toan")
    @ApiResponse(responseCode = "200", description = "Lay chi tiet booking thanh cong")
    @ApiResponse(responseCode = "404", description = "Khong tim thay booking")
    public ResponseEntity<BookingResponse> getBookingDetail(@PathVariable @Positive Integer bookingId) {
        return ResponseEntity.ok(bookingService.getBookingByIdForAdmin(bookingId));
    }

    @PutMapping("/bookings/{bookingId}")
    @Operation(summary = "Admin sua booking chua thanh toan", description = "Cho phep cap nhat thong tin lien he, ghi chu va han thanh toan cua booking pending")
    @ApiResponse(responseCode = "200", description = "Cap nhat booking thanh cong")
    @ApiResponse(responseCode = "400", description = "Booking da thanh toan hoac du lieu khong hop le")
    @ApiResponse(responseCode = "404", description = "Khong tim thay booking")
    public ResponseEntity<BookingResponse> updatePendingBooking(
            @PathVariable @Positive Integer bookingId,
            @Valid @RequestBody AdminUpdateBookingRequest request
    ) {
        return ResponseEntity.ok(bookingService.updatePendingBookingByAdmin(bookingId, request));
    }

    @DeleteMapping("/bookings/{bookingId}")
    @Operation(summary = "Admin huy booking chua thanh toan", description = "Cho phep nhan vien huy booking pending neu khong lien lac duoc voi khach hoac khach yeu cau huy")
    @ApiResponse(responseCode = "200", description = "Huy booking thanh cong")
    @ApiResponse(responseCode = "400", description = "Booking da thanh toan hoac khong o trang thai cho thanh toan")
    @ApiResponse(responseCode = "404", description = "Khong tim thay booking")
    public ResponseEntity<MessageResponse> cancelPendingBookingByAdmin(@PathVariable @Positive Integer bookingId) {
        return ResponseEntity.ok(bookingService.cancelPendingBookingByAdmin(bookingId));
    }

    @GetMapping("/trip-schedules")
    @Operation(summary = "Lay danh sach lich chay mau", description = "Lay danh sach cac lich chay dung de sinh chuyen xe tu dong")
    @ApiResponse(responseCode = "200", description = "Lay danh sach lich chay thanh cong")
    public ResponseEntity<List<AdminTripScheduleResponse>> getTripSchedules() {
        return ResponseEntity.ok(adminService.getTripSchedules());
    }

    @PostMapping("/trip-schedules")
    @Operation(summary = "Tao lich chay mau", description = "Tao lich chay theo tuyen, xe, gio chay va khoang ap dung")
    @ApiResponse(responseCode = "201", description = "Tao lich chay thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu lich chay khong hop le")
    public ResponseEntity<AdminTripScheduleResponse> createTripSchedule(
            @Valid @RequestBody AdminCreateTripScheduleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTripSchedule(request));
    }

    @PutMapping("/trip-schedules/{scheduleId}")
    @Operation(summary = "Cap nhat lich chay mau", description = "Chinh sua tuyen, xe, gio chay va khoang ap dung cua lich chay mau")
    @ApiResponse(responseCode = "200", description = "Cap nhat lich chay thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu lich chay khong hop le")
    @ApiResponse(responseCode = "404", description = "Khong tim thay lich chay")
    public ResponseEntity<AdminTripScheduleResponse> updateTripSchedule(
            @PathVariable @Positive Integer scheduleId,
            @Valid @RequestBody AdminCreateTripScheduleRequest request
    ) {
        return ResponseEntity.ok(adminService.updateTripSchedule(scheduleId, request));
    }

    @DeleteMapping("/trip-schedules/{scheduleId}")
    @Operation(summary = "Xoa lich chay mau", description = "Xoa mau lich chay de dung sinh chuyen tu dong cho cac dot sau")
    @ApiResponse(responseCode = "204", description = "Xoa lich chay thanh cong")
    @ApiResponse(responseCode = "404", description = "Khong tim thay lich chay")
    public ResponseEntity<Void> deleteTripSchedule(@PathVariable @Positive Integer scheduleId) {
        adminService.deleteTripSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/trip-schedules/generate")
    @Operation(summary = "Sinh chuyen xe tu lich", description = "Sinh cac ban ghi Trips va TripSeats tu cac lich chay dang hoat dong trong khoang ngay")
    @ApiResponse(responseCode = "200", description = "Sinh chuyen xe thanh cong")
    @ApiResponse(responseCode = "400", description = "Khoang ngay hoac lich chay khong hop le")
    public ResponseEntity<AdminGeneratedTripsResponse> generateTrips(
            @Valid @RequestBody AdminGenerateTripsRequest request
    ) {
        return ResponseEntity.ok(adminService.generateTripsFromSchedules(request));
    }

    @GetMapping("/routes")
    @Operation(summary = "Lay thong tin tuyen xe", description = "Thong ke cac tuyen xe dang duoc khai thac")
    @ApiResponse(responseCode = "200", description = "Lay thong tin tuyen xe thanh cong")
    public ResponseEntity<AdminRoutesResponse> getRoutes() {
        return ResponseEntity.ok(adminService.getRoutes());
    }

    @PostMapping("/routes")
    @Operation(summary = "Tao tuyen xe moi", description = "Tao tuyen xe voi nhieu diem dung va muc gia theo tung chang")
    @ApiResponse(responseCode = "201", description = "Tao tuyen xe thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu tao tuyen xe khong hop le")
    public ResponseEntity<AdminRouteDetailResponse> createRoute(@Valid @RequestBody AdminCreateRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createRoute(request));
    }

    @PutMapping("/routes/{routeId}")
    @Operation(summary = "Cap nhat tuyen xe", description = "Cap nhat diem dung, khoang cach, thoi gian va bang gia theo chang cua tuyen")
    @ApiResponse(responseCode = "200", description = "Cap nhat tuyen xe thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu tuyen xe khong hop le hoac tuyen da phat sinh lich/chuyen")
    @ApiResponse(responseCode = "404", description = "Khong tim thay tuyen xe")
    public ResponseEntity<AdminRouteDetailResponse> updateRoute(
            @PathVariable @Positive Integer routeId,
            @Valid @RequestBody AdminCreateRouteRequest request
    ) {
        return ResponseEntity.ok(adminService.updateRoute(routeId, request));
    }

    @DeleteMapping("/routes/{routeId}")
    @Operation(summary = "Xoa tuyen xe", description = "Xoa tuyen xe khi tuyen chua duoc gan vao lich chay hoac chuyen xe")
    @ApiResponse(responseCode = "204", description = "Xoa tuyen xe thanh cong")
    @ApiResponse(responseCode = "400", description = "Tuyen da phat sinh lich/chuyen nen khong the xoa")
    @ApiResponse(responseCode = "404", description = "Khong tim thay tuyen xe")
    public ResponseEntity<Void> deleteRoute(@PathVariable @Positive Integer routeId) {
        adminService.deleteRoute(routeId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/routes/{routeId}")
    @Operation(summary = "Lay chi tiet tuyen xe", description = "Lay danh sach diem dung va gia theo chang cua tuyen")
    @ApiResponse(responseCode = "200", description = "Lay chi tiet tuyen xe thanh cong")
    @ApiResponse(responseCode = "404", description = "Khong tim thay tuyen xe")
    public ResponseEntity<AdminRouteDetailResponse> getRouteDetail(@PathVariable @Positive Integer routeId) {
        return ResponseEntity.ok(adminService.getRouteDetail(routeId));
    }

    @PostMapping("/locations")
    @Operation(summary = "Tao dia diem moi", description = "Tao location moi de dung cho diem dau, diem cuoi hoac diem dung tren tuyen")
    @ApiResponse(responseCode = "201", description = "Tao dia diem thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu tao dia diem khong hop le")
    public ResponseEntity<LocationResponse> createLocation(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(locationService.createLocation(request));
    }

    @GetMapping("/fleet")
    @Operation(summary = "Lay thong tin doi xe", description = "Tong hop trang thai phuong tien va hoat dong gan nhat")
    @ApiResponse(responseCode = "200", description = "Lay thong tin doi xe thanh cong")
    public ResponseEntity<AdminFleetResponse> getFleet() {
        return ResponseEntity.ok(adminService.getFleet());
    }

    @PostMapping("/fleet/vehicles")
    @Operation(summary = "Them xe moi", description = "Tao phuong tien moi cho doi xe admin")
    @ApiResponse(responseCode = "201", description = "Them xe thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu xe khong hop le")
    public ResponseEntity<AdminFleetResponse.VehicleItem> createVehicle(
            @Valid @RequestBody AdminUpsertVehicleRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createVehicle(request));
    }

    @PutMapping("/fleet/vehicles/{vehicleId}")
    @Operation(summary = "Cap nhat xe", description = "Cap nhat bien so, hang xe, nam sx, loai xe va trang thai")
    @ApiResponse(responseCode = "200", description = "Cap nhat xe thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu xe khong hop le")
    @ApiResponse(responseCode = "404", description = "Khong tim thay xe")
    public ResponseEntity<AdminFleetResponse.VehicleItem> updateVehicle(
            @PathVariable @Positive Integer vehicleId,
            @Valid @RequestBody AdminUpsertVehicleRequest request
    ) {
        return ResponseEntity.ok(adminService.updateVehicle(vehicleId, request));
    }

    @PatchMapping("/fleet/vehicles/{vehicleId}/status")
    @Operation(summary = "Doi trang thai xe", description = "Cap nhat nhanh trang thai hoat dong cua xe")
    @ApiResponse(responseCode = "200", description = "Cap nhat trang thai xe thanh cong")
    @ApiResponse(responseCode = "400", description = "Trang thai xe khong hop le")
    @ApiResponse(responseCode = "404", description = "Khong tim thay xe")
    public ResponseEntity<AdminFleetResponse.VehicleItem> updateVehicleStatus(
            @PathVariable @Positive Integer vehicleId,
            @Valid @RequestBody AdminUpdateVehicleStatusRequest request
    ) {
        return ResponseEntity.ok(adminService.updateVehicleStatus(vehicleId, request));
    }

    @GetMapping("/staff")
    @Operation(summary = "Lay thong tin nhan su", description = "Lay danh sach nguoi dung va vai tro hien co cho man hinh admin")
    @ApiResponse(responseCode = "200", description = "Lay thong tin nhan su thanh cong")
    public ResponseEntity<AdminStaffResponse> getStaff() {
        return ResponseEntity.ok(adminService.getStaff());
    }
}
