package com.busline.tranmaunhan.service;

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
import com.busline.tranmaunhan.dto.admin.AdminUpdateVehicleStatusRequest;
import com.busline.tranmaunhan.dto.admin.AdminUpsertVehicleRequest;

import java.time.LocalDate;
import java.util.List;

public interface AdminService {

    AdminDashboardResponse getDashboard();

    AdminScheduleResponse getSchedule(LocalDate date, Integer originId, Integer destinationId);

    AdminTripBookingSeatMapResponse getTripBookingSeatMap(Integer tripId, Integer pickupLocationId, Integer dropoffLocationId);

    List<AdminTripScheduleResponse> getTripSchedules();

    AdminTripScheduleResponse createTripSchedule(AdminCreateTripScheduleRequest request);

    AdminTripScheduleResponse updateTripSchedule(Integer scheduleId, AdminCreateTripScheduleRequest request);

    void deleteTripSchedule(Integer scheduleId);

    AdminGeneratedTripsResponse generateTripsFromSchedules(AdminGenerateTripsRequest request);

    AdminRoutesResponse getRoutes();

    AdminRouteDetailResponse createRoute(AdminCreateRouteRequest request);

    AdminRouteDetailResponse updateRoute(Integer routeId, AdminCreateRouteRequest request);

    void deleteRoute(Integer routeId);

    AdminRouteDetailResponse getRouteDetail(Integer routeId);

    AdminFleetResponse getFleet();

    AdminFleetResponse.VehicleItem createVehicle(AdminUpsertVehicleRequest request);

    AdminFleetResponse.VehicleItem updateVehicle(Integer vehicleId, AdminUpsertVehicleRequest request);

    AdminFleetResponse.VehicleItem updateVehicleStatus(Integer vehicleId, AdminUpdateVehicleStatusRequest request);

    AdminStaffResponse getStaff();
}
