package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.dto.trip.TripDetailsResponse;
import com.busline.tranmaunhan.dto.trip.TripDetailSeatLayoutItemResponse;
import com.busline.tranmaunhan.dto.trip.TripSeatMapItemResponse;
import com.busline.tranmaunhan.dto.trip.TripSeatMapResponse;
import com.busline.tranmaunhan.dto.trip.TripSearchResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.busline.tranmaunhan.entity.Trips;
import com.busline.tranmaunhan.repository.RouteSegmentPriceRepository;
import com.busline.tranmaunhan.repository.TripRepository;
import com.busline.tranmaunhan.repository.TripSeatRepository;
import com.busline.tranmaunhan.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final TypeReference<List<String>> ROUTE_STOPS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<TripDetailSeatLayoutItemResponse>> SEAT_LAYOUT_TYPE = new TypeReference<>() {
    };

    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final RouteSegmentPriceRepository routeSegmentPriceRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TripSearchResponse> searchTrips(Integer pickupLocationId, Integer dropoffLocationId, LocalDate departureDate) {
        if (pickupLocationId.equals(dropoffLocationId)) {
            throw new IllegalArgumentException("Diem don va diem tra khong duoc giong nhau");
        }

        List<TripRepository.TripSearchProjection> trips =
                tripRepository.findTripsFullRoute(pickupLocationId, dropoffLocationId, departureDate);
        if (trips.isEmpty()) {
            return List.of();
        }

        List<Integer> tripIds = trips.stream()
                .map(TripRepository.TripSearchProjection::getTripId)
                .toList();
        LinkedHashMap<Integer, Integer> routeIdByTripId = tripRepository.findRouteIdsByTripIds(tripIds).stream()
                .collect(Collectors.toMap(
                        TripRepository.TripRouteProjection::getTripId,
                        TripRepository.TripRouteProjection::getRouteId,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        Set<Integer> routeIds = routeIdByTripId.values().stream().collect(Collectors.toSet());
        LinkedHashMap<Integer, BigDecimal> priceByRouteId = routeSegmentPriceRepository
                .findPricesForRoutes(routeIds, pickupLocationId, dropoffLocationId).stream()
                .collect(Collectors.toMap(
                        RouteSegmentPriceRepository.RouteSegmentPriceProjection::getRouteId,
                        RouteSegmentPriceRepository.RouteSegmentPriceProjection::getPrice,
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        return trips.stream()
                .map(trip -> {
                    Integer routeId = routeIdByTripId.get(trip.getTripId());
                    if (routeId == null) {
                        return null;
                    }

                    BigDecimal price = priceByRouteId.get(routeId);
                    if (price == null) {
                        return null;
                    }

                    return new TripSearchResponse(
                            trip.getTripId(),
                            trip.getDepartureTime(),
                            trip.getRouteOrigin(),
                            trip.getRouteDestination(),
                            trip.getLicensePlate(),
                            trip.getVehicleType(),
                            price
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripDetailsResponse getTripDetails(Integer tripId) {
        TripRepository.TripDetailsProjection tripDetails = tripRepository.findTripDetails(tripId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay chi tiet chuyen xe voi id = " + tripId));

        return new TripDetailsResponse(
                tripDetails.getTripId(),
                toOffsetDateTime(tripDetails.getDepartureTime()),
                tripDetails.getTripStatus(),
                tripDetails.getVehicleId(),
                tripDetails.getLicensePlate(),
                tripDetails.getVehicleBrand(),
                tripDetails.getVehicleTypeName(),
                tripDetails.getTotalSeats(),
                tripDetails.getRouteId(),
                tripDetails.getOriginName(),
                tripDetails.getDestinationName(),
                tripDetails.getTotalDistanceKm(),
                tripDetails.getTotalDurationMinutes(),
                readJson(tripDetails.getRouteStops(), ROUTE_STOPS_TYPE, "route_stops"),
                readJson(tripDetails.getSeatLayout(), SEAT_LAYOUT_TYPE, "seat_layout")
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TripSeatMapResponse getTripSeatMap(Integer tripId, Integer pickupLocationId, Integer dropoffLocationId) {
        Trips trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay chuyen xe voi id = " + tripId));

        List<TripSeatMapItemResponse> seats = tripSeatRepository.findSeatMapByTripId(tripId).stream()
                .map(seat -> new TripSeatMapItemResponse(
                        seat.getTripSeatId(),
                        seat.getSeatTemplateId(),
                        seat.getSeatCode(),
                        seat.getRowIndex(),
                        seat.getColIndex(),
                        seat.getDeck(),
                        seat.getSeatType(),
                        seat.getStatus()
                ))
                .toList();

        BigDecimal segmentPrice = null;
        if (pickupLocationId != null || dropoffLocationId != null) {
            if (pickupLocationId == null || dropoffLocationId == null) {
                throw new IllegalArgumentException("Can truyen day du pickupLocationId va dropoffLocationId de lay gia chang");
            }
            if (pickupLocationId.equals(dropoffLocationId)) {
                throw new IllegalArgumentException("Diem don va diem tra khong duoc giong nhau");
            }

            segmentPrice = resolveSegmentPrice(
                    trip.getRoute().getId(),
                    pickupLocationId,
                    dropoffLocationId
            );
        }

        return new TripSeatMapResponse(
                trip.getId(),
                trip.getDepartureTime(),
                trip.getStatus(),
                trip.getRoute().getId(),
                trip.getRoute().getOrigin().getName(),
                trip.getRoute().getDestination().getName(),
                trip.getVehicle().getId(),
                trip.getVehicle().getLicensePlate(),
                trip.getVehicle().getVehicleType().getTypeName(),
                trip.getVehicle().getVehicleType().getTotalSeats(),
                pickupLocationId,
                dropoffLocationId,
                segmentPrice,
                seats.size(),
                seats
        );
    }

    private BigDecimal resolveSegmentPrice(Integer routeId, Integer pickupLocationId, Integer dropoffLocationId) {
        return routeSegmentPriceRepository.findPriceByRouteAndLocations(routeId, pickupLocationId, dropoffLocationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Khong tim thay gia ve cho chang da chon tren tuyen id = " + routeId
                ));
    }

    private <T> T readJson(String json, TypeReference<T> typeReference, String fieldName) {
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("Du lieu " + fieldName + " tu database dang rong");
        }

        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Khong the doc du lieu " + fieldName + " tu database", ex);
        }
    }

    private OffsetDateTime toOffsetDateTime(java.time.Instant value) {
        if (value == null) {
            return null;
        }
        return value.atZone(APP_ZONE_ID).toOffsetDateTime();
    }
}
