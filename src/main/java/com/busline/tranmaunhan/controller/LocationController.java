package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.location.LocationResponse;
import com.busline.tranmaunhan.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name = "Locations", description = "Thông tin điểm đón trả, trả và trạm dừng")
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    @Operation(summary = "Lấy danh sách location", description = "Trả về danh sách địa điểm")
    @ApiResponse(responseCode = "200", description = "Lấy danh sách location thành công")
    public ResponseEntity<List<LocationResponse>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }
}
