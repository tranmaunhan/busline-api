package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.dto.trip.PopularRouteResponse;
import com.busline.tranmaunhan.dto.trip.TripDetailsResponse;
import com.busline.tranmaunhan.dto.trip.TripDetailSeatLayoutItemResponse;
import com.busline.tranmaunhan.dto.trip.TripSeatMapItemResponse;
import com.busline.tranmaunhan.dto.trip.TripSeatMapResponse;
import com.busline.tranmaunhan.dto.trip.TripSearchResponse;
import com.busline.tranmaunhan.entity.Routes;
import com.busline.tranmaunhan.entity.Trips;
import com.busline.tranmaunhan.repository.RouteSegmentPriceRepository;
import com.busline.tranmaunhan.repository.RouteStopRepository;
import com.busline.tranmaunhan.repository.RoutesRepository;
import com.busline.tranmaunhan.repository.TicketRepository;
import com.busline.tranmaunhan.repository.TripRepository;
import com.busline.tranmaunhan.repository.TripScheduleRepository;
import com.busline.tranmaunhan.repository.TripSeatRepository;
import com.busline.tranmaunhan.service.ExpiredBookingCleanupService;
import com.busline.tranmaunhan.service.TripService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripServiceImpl implements TripService {

    private static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final int DEFAULT_POPULAR_ROUTE_LIMIT = 4;
    private static final int MAX_POPULAR_ROUTE_LIMIT = 12;
    private static final int SEAT_STATUS_AVAILABLE = 0;
    private static final TypeReference<List<String>> ROUTE_STOPS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<TripDetailSeatLayoutItemResponse>> SEAT_LAYOUT_TYPE = new TypeReference<>() {
    };

    private final TripRepository tripRepository;
    private final TripSeatRepository tripSeatRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final TicketRepository ticketRepository;
    private final RoutesRepository routesRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteSegmentPriceRepository routeSegmentPriceRepository;
    private final ObjectMapper objectMapper;
    private final ExpiredBookingCleanupService expiredBookingCleanupService;

    @Override
    @Transactional(readOnly = true)
    public List<TripSearchResponse> searchTrips(Integer pickupLocationId, Integer dropoffLocationId,
            LocalDate departureDate) {
        expiredBookingCleanupService.cleanupExpiredPendingBookings();

        if (pickupLocationId.equals(dropoffLocationId)) {
            throw new IllegalArgumentException("Diem don va diem tra khong duoc giong nhau");
        }

        List<TripRepository.TripSearchProjection> trips = tripRepository.findTripsFullRoute(pickupLocationId,
                dropoffLocationId, departureDate);
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
                        LinkedHashMap::new));
        Set<Integer> routeIds = routeIdByTripId.values().stream().collect(Collectors.toSet());
        LinkedHashMap<Integer, BigDecimal> priceByRouteId = routeSegmentPriceRepository
                .findPricesForRoutes(routeIds, pickupLocationId, dropoffLocationId).stream()
                .collect(Collectors.toMap(
                        RouteSegmentPriceRepository.RouteSegmentPriceProjection::getRouteId,
                        RouteSegmentPriceRepository.RouteSegmentPriceProjection::getPrice,
                        (left, right) -> left,
                        LinkedHashMap::new));
        Map<String, RouteStopTiming> stopTimingByRouteAndLocation = routeStopRepository
                .findStopTimingsByRouteIdsAndLocationIds(routeIds, Set.of(pickupLocationId, dropoffLocationId)).stream()
                .collect(Collectors.toMap(
                        projection -> buildRouteStopKey(projection.getRouteId(), projection.getLocationId()),
                        projection -> new RouteStopTiming(
                                projection.getRouteId(),
                                projection.getLocationId(),
                                projection.getLocationName(),
                                projection.getStopOrder(),
                                projection.getEstimatedTimeFromStartMinutes()
                        ),
                        (left, right) -> left,
                        HashMap::new
                ));
        Map<Integer, Integer> availableSeatsByTripId = tripSeatRepository.countAvailableSeatsByTripIds(tripIds).stream()
                .collect(Collectors.toMap(
                        TripSeatRepository.TripAvailableSeatProjection::getTripId,
                        projection -> projection.getAvailableSeatCount() == null ? 0 : projection.getAvailableSeatCount().intValue(),
                        (left, right) -> left,
                        HashMap::new
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

                    RouteStopTiming pickupStop = stopTimingByRouteAndLocation.get(buildRouteStopKey(routeId, pickupLocationId));
                    RouteStopTiming dropoffStop = stopTimingByRouteAndLocation.get(buildRouteStopKey(routeId, dropoffLocationId));
                    if (pickupStop == null || dropoffStop == null) {
                        return null;
                    }
                    if (pickupStop.stopOrder() == null || dropoffStop.stopOrder() == null
                            || pickupStop.stopOrder() >= dropoffStop.stopOrder()) {
                        return null;
                    }

                    OffsetDateTime tripDepartureTime = toOffsetDateTime(trip.getDepartureTime());
                    if (tripDepartureTime == null) {
                        return null;
                    }
                    OffsetDateTime pickupTime = tripDepartureTime.plusMinutes(safeMinutes(pickupStop.estimatedTimeFromStartMinutes()));
                    OffsetDateTime dropoffTime = tripDepartureTime.plusMinutes(safeMinutes(dropoffStop.estimatedTimeFromStartMinutes()));

                    return new TripSearchResponse(
                            trip.getTripId(),
                            tripDepartureTime,
                            pickupTime,
                            dropoffTime,
                            trip.getRouteOrigin(),
                            trip.getRouteDestination(),
                            pickupLocationId,
                            pickupStop.locationName(),
                            dropoffLocationId,
                            dropoffStop.locationName(),
                            trip.getLicensePlate(),
                            trip.getVehicleType(),
                            availableSeatsByTripId.getOrDefault(trip.getTripId(), 0),
                            Math.max(safeMinutes(dropoffStop.estimatedTimeFromStartMinutes())
                                    - safeMinutes(pickupStop.estimatedTimeFromStartMinutes()), 0),
                            price);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PopularRouteResponse> getPopularRoutes(Integer limit) {
        expiredBookingCleanupService.cleanupExpiredPendingBookings();

        int resolvedLimit = Math.max(1, Math.min(limit == null ? DEFAULT_POPULAR_ROUTE_LIMIT : limit, MAX_POPULAR_ROUTE_LIMIT));
        LocalDate today = LocalDate.now(APP_ZONE_ID);
        OffsetDateTime recentStart = today.minusDays(29).atStartOfDay(APP_ZONE_ID).toOffsetDateTime();
        OffsetDateTime recentEnd = today.plusDays(1).atStartOfDay(APP_ZONE_ID).toOffsetDateTime();

        Map<Integer, Long> dailyTripCountByRouteId = tripScheduleRepository.findActiveScheduleCountsForDate(today).stream()
                .collect(Collectors.toMap(
                        TripScheduleRepository.RouteScheduleCountProjection::getRouteId,
                        projection -> projection.getScheduleCount() == null ? 0L : projection.getScheduleCount(),
                        Long::max,
                        LinkedHashMap::new
                ));

        if (dailyTripCountByRouteId.isEmpty()) {
            return List.of();
        }

        Set<Integer> routeIds = dailyTripCountByRouteId.keySet();
        Map<Integer, Long> recentTripCountByRouteId = tripRepository.findAdminTripsByDepartureTimeBetween(recentStart, recentEnd).stream()
                .filter(trip -> routeIds.contains(trip.getRouteId()))
                .collect(Collectors.groupingBy(
                        TripRepository.AdminTripProjection::getRouteId,
                        Collectors.counting()
                ));

        Map<Integer, Long> recentTicketCountByRouteId = ticketRepository.findRouteTicketDetailsByDepartureTimeBetween(recentStart, recentEnd).stream()
                .filter(ticket -> routeIds.contains(ticket.getRouteId()))
                .collect(Collectors.groupingBy(
                        TicketRepository.RouteTicketDetailProjection::getRouteId,
                        Collectors.counting()
                ));

        Map<Integer, BigDecimal> startingPriceByRouteId = routeSegmentPriceRepository.findMinimumPricesByRouteIds(routeIds).stream()
                .collect(Collectors.toMap(
                        RouteSegmentPriceRepository.RouteMinimumPriceProjection::getRouteId,
                        RouteSegmentPriceRepository.RouteMinimumPriceProjection::getMinimumPrice,
                        (left, right) -> left,
                        HashMap::new
                ));

        return routesRepository.findAllById(routeIds).stream()
                .filter(route -> dailyTripCountByRouteId.getOrDefault(route.getId(), 0L) > 0)
                .sorted(
                        Comparator.<Routes>comparingLong(route -> recentTicketCountByRouteId.getOrDefault(route.getId(), 0L)).reversed()
                                .thenComparing(Comparator.comparingLong(
                                        (Routes route) -> recentTripCountByRouteId.getOrDefault(route.getId(), 0L)).reversed())
                                .thenComparing(Comparator.comparingLong(
                                        (Routes route) -> dailyTripCountByRouteId.getOrDefault(route.getId(), 0L)).reversed())
                                .thenComparing(route -> defaultText(route.getOrigin() == null ? null : route.getOrigin().getName()))
                                .thenComparing(route -> defaultText(route.getDestination() == null ? null : route.getDestination().getName()))
                )
                .limit(resolvedLimit)
                .map(route -> new PopularRouteResponse(
                        route.getId(),
                        defaultText(route.getOrigin() == null ? null : route.getOrigin().getName()),
                        defaultText(route.getDestination() == null ? null : route.getDestination().getName()),
                        route.getEstimatedDurationMinutes(),
                        startingPriceByRouteId.get(route.getId()),
                        dailyTripCountByRouteId.getOrDefault(route.getId(), 0L)
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TripDetailsResponse getTripDetails(Integer tripId) {
        expiredBookingCleanupService.cleanupExpiredPendingBookings();

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
                readJson(tripDetails.getSeatLayout(), SEAT_LAYOUT_TYPE, "seat_layout"));
    }

    @Override
    @Transactional(readOnly = true)
    public TripSeatMapResponse getTripSeatMap(Integer tripId, Integer pickupLocationId, Integer dropoffLocationId) {
        expiredBookingCleanupService.cleanupExpiredPendingBookings();

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
                        seat.getStatus()))
                .toList();

        BigDecimal segmentPrice = null;
        if (pickupLocationId != null || dropoffLocationId != null) {
            if (pickupLocationId == null || dropoffLocationId == null) {
                throw new IllegalArgumentException(
                        "Can truyen day du pickupLocationId va dropoffLocationId de lay gia chang");
            }
            if (pickupLocationId.equals(dropoffLocationId)) {
                throw new IllegalArgumentException("Diem don va diem tra khong duoc giong nhau");
            }

            segmentPrice = resolveSegmentPrice(
                    trip.getRoute().getId(),
                    pickupLocationId,
                    dropoffLocationId);
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
                seats);
    }

    private BigDecimal resolveSegmentPrice(Integer routeId, Integer pickupLocationId, Integer dropoffLocationId) {
        return routeSegmentPriceRepository.findPriceByRouteAndLocations(routeId, pickupLocationId, dropoffLocationId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Khong tim thay gia ve cho chang da chon tren tuyen id = " + routeId));
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

    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }

        return value.atZone(APP_ZONE_ID).toOffsetDateTime();
    }

    private String buildRouteStopKey(Integer routeId, Integer locationId) {
        return routeId + "|" + locationId;
    }

    private int safeMinutes(Integer value) {
        return value == null ? 0 : Math.max(value, 0);
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private record RouteStopTiming(
            Integer routeId,
            Integer locationId,
            String locationName,
            Integer stopOrder,
            Integer estimatedTimeFromStartMinutes
    ) {
    }
}
