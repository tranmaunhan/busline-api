package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.trip.TripDetailsResponse;
import com.busline.tranmaunhan.dto.trip.TripSearchResponse;
import com.busline.tranmaunhan.dto.trip.TripSeatMapResponse;
import com.busline.tranmaunhan.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Tìm chuyến xe theo điểm đon, điểm trả và ngày đi")
public class TripController {

        private final TripService tripService;

        @GetMapping("/search")
        @Operation(summary = "Tìm chuyến xe theo ngày", description = "Tra ve danh sach chuyen xe theo diem don, diem tra va ngay khoi hanh, kem gia theo chang da chon")
        @ApiResponse(responseCode = "200", description = "Tìm chuyến xe thành công")
        @ApiResponse(responseCode = "400", description = "Tham số tìm kiếm không hợp lệ")
        public ResponseEntity<List<TripSearchResponse>> searchTrips(
                        @RequestParam @NotNull @Positive Integer pickupLocationId,
                        @RequestParam @NotNull @Positive Integer dropoffLocationId,
                        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureDate) {
                return ResponseEntity.ok(tripService.searchTrips(pickupLocationId, dropoffLocationId, departureDate));
        }

        @GetMapping("/{tripId}/details")
        @Operation(summary = "Lấy chi tiết chuyến xe", description = "Trả về thông tin chi tiết, danh sách điểm dừng và sơ đồ ghế của chuyến xe")
        @ApiResponse(responseCode = "200", description = "Lấy chi tiết chuyến xe thành công")
        @ApiResponse(responseCode = "400", description = "Trip id không hợp lệ")
        @ApiResponse(responseCode = "404", description = "Không tìm thấy chi tiết chuyến xe")
        public ResponseEntity<TripDetailsResponse> getTripDetails(@PathVariable @Positive Integer tripId) {
                return ResponseEntity.ok(tripService.getTripDetails(tripId));
        }

        @GetMapping("/{tripId}/seat-map")
        @Operation(summary = "Lấy sơ đồ chuyến của chuyến xe", description = "Trả về thông tin chuyến xe")
        @ApiResponse(responseCode = "200", description = "Lấy sơ đồ ghế thành công")
        @ApiResponse(responseCode = "400", description = "Trip id không hợp lệ")
        @ApiResponse(responseCode = "404", description = "Không tìm thấy chuyến xe")
        public ResponseEntity<TripSeatMapResponse> getTripSeatMap(
                        @PathVariable @Positive Integer tripId,
                        @RequestParam(required = false) @Positive Integer pickupLocationId,
                        @RequestParam(required = false) @Positive Integer dropoffLocationId) {
                return ResponseEntity.ok(tripService.getTripSeatMap(tripId, pickupLocationId, dropoffLocationId));
        }
}
