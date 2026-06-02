package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.trip.TripDetailsResponse;
import com.busline.tranmaunhan.dto.trip.TripSeatMapResponse;
import com.busline.tranmaunhan.dto.trip.TripSearchResponse;

import java.time.LocalDate;
import java.util.List;

public interface TripService {

    List<TripSearchResponse> searchTrips(Integer pickupLocationId, Integer dropoffLocationId, LocalDate departureDate);

    TripDetailsResponse getTripDetails(Integer tripId);

    TripSeatMapResponse getTripSeatMap(Integer tripId, Integer pickupLocationId, Integer dropoffLocationId);
}
