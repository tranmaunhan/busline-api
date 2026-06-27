package com.busline.tranmaunhan.service.serviceImpl;

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
import com.busline.tranmaunhan.entity.Bookings;
import com.busline.tranmaunhan.entity.Locations;
import com.busline.tranmaunhan.entity.RouteSegmentPrices;
import com.busline.tranmaunhan.entity.RouteStops;
import com.busline.tranmaunhan.entity.Routes;
import com.busline.tranmaunhan.entity.SeatTemplates;
import com.busline.tranmaunhan.entity.Tickets;
import com.busline.tranmaunhan.entity.TripSchedules;
import com.busline.tranmaunhan.entity.TripSeats;
import com.busline.tranmaunhan.entity.Trips;
import com.busline.tranmaunhan.entity.Users;
import com.busline.tranmaunhan.entity.Vehicles;
import com.busline.tranmaunhan.entity.VehicleTypes;
import com.busline.tranmaunhan.repository.BookingRepository;
import com.busline.tranmaunhan.repository.LocationsRepository;
import com.busline.tranmaunhan.repository.RouteSegmentPriceRepository;
import com.busline.tranmaunhan.repository.RouteStopRepository;
import com.busline.tranmaunhan.repository.RoutesRepository;
import com.busline.tranmaunhan.repository.SeatTemplateRepository;
import com.busline.tranmaunhan.repository.TicketRepository;
import com.busline.tranmaunhan.repository.TripRepository;
import com.busline.tranmaunhan.repository.TripScheduleRepository;
import com.busline.tranmaunhan.repository.TripSeatRepository;
import com.busline.tranmaunhan.repository.UsersRepository;
import com.busline.tranmaunhan.repository.VehicleRepository;
import com.busline.tranmaunhan.repository.VehicleTypeRepository;
import com.busline.tranmaunhan.service.AdminService;
import com.busline.tranmaunhan.service.ExpiredBookingCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final Locale VIETNAMESE = Locale.forLanguageTag("vi-VN");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter DATE_HEADING_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int BOOKING_STATUS_PENDING = 0;
    private static final int BOOKING_STATUS_CONFIRMED = 1;
    private static final int SCHEDULE_STATUS_INACTIVE = 0;
    private static final int SCHEDULE_STATUS_ACTIVE = 1;
    private static final int SEAT_STATUS_AVAILABLE = 0;
    private static final int TRIP_STATUS_SCHEDULED = 0;
    private static final String VEHICLE_STATUS_ACTIVE = "ACTIVE";
    private static final String VEHICLE_STATUS_MAINTENANCE = "MAINTENANCE";
    private static final String VEHICLE_STATUS_RESERVE = "RESERVE";

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final TripScheduleRepository tripScheduleRepository;
    private final TripSeatRepository tripSeatRepository;
    private final TicketRepository ticketRepository;
    private final RoutesRepository routesRepository;
    private final LocationsRepository locationsRepository;
    private final RouteStopRepository routeStopRepository;
    private final RouteSegmentPriceRepository routeSegmentPriceRepository;
    private final SeatTemplateRepository seatTemplateRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final UsersRepository usersRepository;
    private final ExpiredBookingCleanupService expiredBookingCleanupService;

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);
        LocalDate today = now.toLocalDate();
        OffsetDateTime startOfToday = startOfDay(today);
        OffsetDateTime startOfTomorrow = startOfDay(today.plusDays(1));
        OffsetDateTime startOfWeek = startOfDay(today.minusDays(6));

        List<BookingRepository.AdminBookingMetricProjection> todayBookingMetrics =
                bookingRepository.findAdminMetricsByBookingTimeBetween(startOfToday, startOfTomorrow);
        List<BookingRepository.AdminBookingMetricProjection> weekBookingMetrics =
                bookingRepository.findAdminMetricsByBookingTimeBetween(startOfWeek, startOfTomorrow);
        List<TripRepository.AdminTripProjection> todayTrips =
                tripRepository.findAdminTripsByDepartureTimeBetween(startOfToday, startOfTomorrow);
        List<TripRepository.AdminTripProjection> weekTrips =
                tripRepository.findAdminTripsByDepartureTimeBetween(startOfWeek, startOfTomorrow);

        Map<Integer, Long> todayTicketCounts = getTicketCounts(todayTrips);
        Map<Integer, Long> weekTicketCounts = getTicketCounts(weekTrips);

        BigDecimal todayRevenue = todayBookingMetrics.stream()
                .filter(metric -> BOOKING_STATUS_CONFIRMED == safeInt(metric.getStatus()))
                .map(metric -> defaultAmount(metric.getTotalAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingBookings = todayBookingMetrics.stream()
                .filter(metric -> BOOKING_STATUS_PENDING == safeInt(metric.getStatus()))
                .count();

        long totalTicketsToday = todayTicketCounts.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        long totalTripsToday = todayTrips.size();
        long departedTrips = todayTrips.stream()
                .filter(trip -> trip.getDepartureTime() != null && !trip.getDepartureTime().isAfter(now))
                .count();
        long upcomingTripsCount = Math.max(totalTripsToday - departedTrips, 0);

        long totalCapacityToday = todayTrips.stream()
                .mapToLong(trip -> safeInt(trip.getTotalSeats()))
                .sum();
        double occupancyRate = totalCapacityToday == 0
                ? 0D
                : (double) totalTicketsToday * 100D / totalCapacityToday;

        List<VehicleRepository.AdminVehicleProjection> vehicles = vehicleRepository.findAdminVehicles();
        long maintenanceVehicles = vehicles.stream()
                .filter(vehicle -> isMaintenanceStatus(vehicle.getStatus()))
                .count();
        long reserveVehicles = vehicles.stream()
                .filter(vehicle -> isReserveStatus(vehicle.getStatus()))
                .count();

        List<AdminDashboardResponse.OverviewStat> overviewStats = List.of(
                new AdminDashboardResponse.OverviewStat(
                        "Doanh thu",
                        formatCurrencyCompact(todayRevenue),
                        buildTrendCaption(todayRevenue, weekBookingMetrics)
                ),
                new AdminDashboardResponse.OverviewStat(
                        "Ve giu/ban",
                        Long.toString(totalTicketsToday),
                        totalTripsToday == 0
                                ? "Chua co chuyen nao trong ngay"
                                : "Trung binh " + formatOneDecimal((double) totalTicketsToday / totalTripsToday) + " ve/chuyen"
                ),
                new AdminDashboardResponse.OverviewStat(
                        "Chuyen hom nay",
                        Long.toString(totalTripsToday),
                        departedTrips + " chuyen da roi ben"
                ),
                new AdminDashboardResponse.OverviewStat(
                        "Cho thanh toan",
                        Long.toString(pendingBookings),
                        pendingBookings == 0
                                ? "Khong co booking treo trong ngay"
                                : pendingBookings + " booking dang giu cho"
                )
        );

        List<AdminDashboardResponse.MetricCard> metricCards = List.of(
                new AdminDashboardResponse.MetricCard(
                        "Doanh thu hom nay",
                        formatCurrency(todayRevenue),
                        "Tong gia tri booking da thanh toan trong ngay",
                        "accent"
                ),
                new AdminDashboardResponse.MetricCard(
                        "So ve dang giu/ban",
                        Long.toString(totalTicketsToday),
                        "Tinh theo toan bo ticket con ton tai tren cac chuyen trong ngay",
                        "sky"
                ),
                new AdminDashboardResponse.MetricCard(
                        "So chuyen hom nay",
                        Long.toString(totalTripsToday),
                        departedTrips + " chuyen da xuat ben, " + upcomingTripsCount + " chuyen con lai",
                        "mint"
                ),
                new AdminDashboardResponse.MetricCard(
                        "Ti le lap day",
                        formatPercent(occupancyRate),
                        totalCapacityToday == 0
                                ? "Chua co du lieu ghe cho cac chuyen trong ngay"
                                : totalTicketsToday + "/" + totalCapacityToday + " ghe da duoc giu hoac thanh toan",
                        "gold"
                ),
                new AdminDashboardResponse.MetricCard(
                        "Don cho thanh toan",
                        Long.toString(pendingBookings),
                        pendingBookings == 0
                                ? "Khong co don nao can nhac thanh toan"
                                : "Can theo doi de tranh het han giu ghe",
                        "rose"
                ),
                new AdminDashboardResponse.MetricCard(
                        "Xe can theo doi",
                        Long.toString(maintenanceVehicles + reserveVehicles),
                        maintenanceVehicles + " xe bao tri, " + reserveVehicles + " xe du phong",
                        "slate"
                )
        );

        Map<LocalDate, BigDecimal> revenueByDate = weekBookingMetrics.stream()
                .filter(metric -> BOOKING_STATUS_CONFIRMED == safeInt(metric.getStatus()))
                .collect(Collectors.groupingBy(
                        metric -> metric.getBookingTime().atZoneSameInstant(APP_ZONE).toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                metric -> defaultAmount(metric.getTotalAmount()),
                                BigDecimal::add
                        )
                ));

        List<AdminDashboardResponse.RevenuePoint> revenueSeries = new ArrayList<>();
        for (int dayOffset = 6; dayOffset >= 0; dayOffset--) {
            LocalDate date = today.minusDays(dayOffset);
            revenueSeries.add(new AdminDashboardResponse.RevenuePoint(
                    shortDayLabel(date.getDayOfWeek()),
                    revenueByDate.getOrDefault(date, BigDecimal.ZERO)
            ));
        }

        Map<Integer, RouteAggregate> routeAggregates = buildRouteAggregates(
                weekTrips,
                weekTicketCounts,
                ticketRepository.findRouteTicketDetailsByDepartureTimeBetween(startOfWeek, startOfTomorrow)
        );

        List<AdminDashboardResponse.RouteHighlight> topRoutes = routeAggregates.values().stream()
                .sorted(Comparator
                        .comparingLong(RouteAggregate::tickets).reversed()
                        .thenComparing(RouteAggregate::revenue, Comparator.reverseOrder()))
                .limit(4)
                .map(aggregate -> new AdminDashboardResponse.RouteHighlight(
                        aggregate.name(),
                        aggregate.tickets(),
                        aggregate.revenue(),
                        aggregate.occupancyRate()
                ))
                .toList();

        OffsetDateTime upcomingUntil = now.plusMinutes(90);
        List<TripRepository.AdminTripProjection> upcomingTripProjections =
                tripRepository.findAdminTripsByDepartureTimeBetween(now, upcomingUntil);
        Map<Integer, Long> upcomingTicketCounts = getTicketCounts(upcomingTripProjections);
        List<AdminDashboardResponse.UpcomingTrip> upcomingTrips = upcomingTripProjections.stream()
                .map(trip -> {
                    long bookedSeats = upcomingTicketCounts.getOrDefault(trip.getTripId(), 0L);
                    int totalSeats = safeInt(trip.getTotalSeats());
                    int emptySeats = Math.max(totalSeats - (int) bookedSeats, 0);
                    return new AdminDashboardResponse.UpcomingTrip(
                            trip.getTripId(),
                            "TRIP-" + trip.getTripId(),
                            buildRouteName(trip.getOriginName(), trip.getDestinationName()),
                            formatTime(trip.getDepartureTime()),
                            "Chua cau hinh ben",
                            bookedSeats + "/" + totalSeats + " ghe",
                            "Chua gan tai xe",
                            resolveUpcomingStatus(trip.getDepartureTime(), emptySeats)
                    );
                })
                .toList();

        List<Integer> latestBookingIds = bookingRepository.findLatestBookingIds(PageRequest.of(0, 6));
        List<AdminDashboardResponse.RecentBooking> latestBookings = latestBookingIds.isEmpty()
                ? List.of()
                : bookingRepository.findByIdInWithAdminDetails(latestBookingIds).stream()
                .sorted(Comparator.comparing(Bookings::getBookingTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toRecentBooking)
                .toList();

        List<AdminDashboardResponse.AlertItem> alerts = buildAlerts(
                pendingBookings,
                maintenanceVehicles,
                reserveVehicles,
                topRoutes
        );

        return new AdminDashboardResponse(
                "Cap nhat luc " + now.format(TIME_FORMATTER),
                overviewStats,
                metricCards,
                revenueSeries,
                topRoutes,
                upcomingTrips,
                latestBookings,
                alerts
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminScheduleResponse getSchedule(LocalDate date, Integer originId, Integer destinationId) {
        expiredBookingCleanupService.cleanupExpiredPendingBookings();

        LocalDate today = LocalDate.now(APP_ZONE);
        LocalDate selectedDate = date == null ? today : date;
        OffsetDateTime start = startOfDay(selectedDate);
        OffsetDateTime end = startOfDay(selectedDate.plusDays(1));

        List<TripRepository.AdminTripProjection> trips = tripRepository.findAdminTripsByDepartureTimeBetween(start, end)
                .stream()
                .filter(trip -> originId == null || Objects.equals(trip.getOriginId(), originId))
                .filter(trip -> destinationId == null || Objects.equals(trip.getDestinationId(), destinationId))
                .toList();

        Map<Integer, Long> ticketCounts = getTicketCounts(trips);
        List<TimeSlotDefinition> slots = List.of(
                new TimeSlotDefinition("05:00 - 08:00", "Ca sang som", LocalTime.of(5, 0), LocalTime.of(8, 0)),
                new TimeSlotDefinition("08:00 - 11:00", "Ca cao diem sang", LocalTime.of(8, 0), LocalTime.of(11, 0)),
                new TimeSlotDefinition("11:00 - 14:00", "Ca trua", LocalTime.of(11, 0), LocalTime.of(14, 0)),
                new TimeSlotDefinition("14:00 - 17:00", "Ca chieu", LocalTime.of(14, 0), LocalTime.of(17, 0)),
                new TimeSlotDefinition("17:00 - 23:59", "Ca toi", LocalTime.of(17, 0), LocalTime.MAX)
        );

        List<AdminScheduleResponse.TimeSlotColumn> columns = slots.stream()
                .map(slot -> new AdminScheduleResponse.TimeSlotColumn(
                        slot.label(),
                        slot.subtitle(),
                        trips.stream()
                                .filter(trip -> slot.matches(trip.getDepartureTime()))
                                .map(trip -> {
                                    int totalSeats = safeInt(trip.getTotalSeats());
                                    int emptySeats = Math.max(totalSeats - ticketCounts.getOrDefault(trip.getTripId(), 0L).intValue(), 0);
                                    return new AdminScheduleResponse.TripItem(
                                            trip.getTripId(),
                                            formatTime(trip.getDepartureTime()),
                                            trip.getOriginId(),
                                            trip.getOriginName(),
                                            trip.getDestinationId(),
                                            trip.getDestinationName(),
                                            emptySeats,
                                            defaultString(trip.getLicensePlate(), "Chua gan xe")
                                    );
                                })
                                .toList()
                ))
                .toList();

        List<AdminScheduleResponse.DayOption> days = buildScheduleDayOptions(today, selectedDate);
        String summary = trips.isEmpty()
                ? "Chua co chuyen nao phu hop voi bo loc da chon."
                : "Co " + trips.size() + " chuyen trong ngay, khung dong nhat la " + findBusiestSlot(columns) + ".";

        return new AdminScheduleResponse(
                selectedDate.toString(),
                "Lich chay ngay " + selectedDate.format(DATE_HEADING_FORMATTER),
                summary,
                days,
                locationsRepository.findAllByOrderByNameAsc().stream()
                        .map(location -> new AdminScheduleResponse.LocationOption(location.getId(), location.getName()))
                        .toList(),
                columns
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTripBookingSeatMapResponse getTripBookingSeatMap(
            Integer tripId,
            Integer pickupLocationId,
            Integer dropoffLocationId
    ) {
        expiredBookingCleanupService.cleanupExpiredPendingBookings();

        Trips trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay chuyen voi id = " + tripId));

        List<RouteStops> routeStops = routeStopRepository.findAllByRouteIdOrderByStopOrderAsc(trip.getRoute().getId());
        if (routeStops.size() < 2) {
            throw new IllegalStateException("Tuyen cua chuyen xe chua co du diem dung");
        }

        BigDecimal segmentPrice = null;
        if (pickupLocationId != null || dropoffLocationId != null) {
            if (pickupLocationId == null || dropoffLocationId == null) {
                throw new IllegalArgumentException("Can chon day du diem don va diem tra");
            }

            if (pickupLocationId.equals(dropoffLocationId)) {
                throw new IllegalArgumentException("Diem don va diem tra khong duoc giong nhau");
            }

            RouteStops pickupStop = routeStops.stream()
                    .filter(stop -> stop.getLocation() != null && Objects.equals(stop.getLocation().getId(), pickupLocationId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Diem don khong thuoc tuyen xe"));

            RouteStops dropoffStop = routeStops.stream()
                    .filter(stop -> stop.getLocation() != null && Objects.equals(stop.getLocation().getId(), dropoffLocationId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Diem tra khong thuoc tuyen xe"));

            if (safeInt(pickupStop.getStopOrder()) >= safeInt(dropoffStop.getStopOrder())) {
                throw new IllegalArgumentException("Diem don phai dung truoc diem tra tren tuyen");
            }

            segmentPrice = routeSegmentPriceRepository.findPriceByRouteAndLocations(
                            trip.getRoute().getId(),
                            pickupLocationId,
                            dropoffLocationId)
                    .orElseThrow(() -> new IllegalArgumentException("Khong tim thay gia chang cho cap diem da chon"));
        }

        List<AdminTripBookingSeatMapResponse.StopOption> stopOptions = routeStops.stream()
                .map(stop -> new AdminTripBookingSeatMapResponse.StopOption(
                        stop.getLocation() == null ? null : stop.getLocation().getId(),
                        stop.getLocation() == null ? "Chua ro diem dung" : defaultString(stop.getLocation().getName(), "Chua ro diem dung"),
                        stop.getStopOrder(),
                        stop.getEstimatedTimeFromStartMinutes()
                ))
                .toList();

        List<AdminTripBookingSeatMapResponse.SeatItem> seatItems = tripSeatRepository.findAdminSeatMapByTripId(tripId).stream()
                .map(seat -> new AdminTripBookingSeatMapResponse.SeatItem(
                        seat.getTripSeatId(),
                        seat.getSeatTemplateId(),
                        seat.getSeatCode(),
                        seat.getRowIndex(),
                        seat.getColIndex(),
                        seat.getDeck(),
                        seat.getSeatType(),
                        seat.getStatus(),
                        seat.getBookingId(),
                        seat.getBookingCode(),
                        seat.getBookingStatus(),
                        defaultString(seat.getContactName(), null),
                        defaultString(seat.getContactPhone(), null)
                ))
                .toList();

        return new AdminTripBookingSeatMapResponse(
                trip.getId(),
                trip.getDepartureTime(),
                trip.getStatus(),
                trip.getRoute().getId(),
                trip.getRoute().getOrigin() == null ? "Chua ro diem di" : defaultString(trip.getRoute().getOrigin().getName(), "Chua ro diem di"),
                trip.getRoute().getDestination() == null ? "Chua ro diem den" : defaultString(trip.getRoute().getDestination().getName(), "Chua ro diem den"),
                trip.getVehicle() == null ? null : trip.getVehicle().getId(),
                trip.getVehicle() == null ? "Chua gan xe" : defaultString(trip.getVehicle().getLicensePlate(), "Chua gan xe"),
                trip.getVehicle() == null || trip.getVehicle().getVehicleType() == null
                        ? "Chua ro loai xe"
                        : defaultString(trip.getVehicle().getVehicleType().getTypeName(), "Chua ro loai xe"),
                trip.getVehicle() == null || trip.getVehicle().getVehicleType() == null
                        ? seatItems.size()
                        : safeInt(trip.getVehicle().getVehicleType().getTotalSeats()),
                pickupLocationId,
                dropoffLocationId,
                segmentPrice,
                stopOptions,
                seatItems
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminTripScheduleResponse> getTripSchedules() {
        return tripScheduleRepository.findAllByOrderByCreatedAtDescIdDesc().stream()
                .map(this::toTripScheduleResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminTripScheduleResponse getTripScheduleById(Integer scheduleId) {
        TripSchedules tripSchedule = tripScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay lich chay voi id = " + scheduleId));

        return toTripScheduleResponse(tripSchedule);
    }

    @Override
    @Transactional
    public AdminTripScheduleResponse createTripSchedule(AdminCreateTripScheduleRequest request) {
        validateTripScheduleRequest(request);

        Routes route = routesRepository.findById(request.routeId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay tuyen voi id = " + request.routeId()));
        Vehicles vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay xe voi id = " + request.vehicleId()));

        validateVehicleCanRunRoute(vehicle);

        OffsetDateTime now = OffsetDateTime.now(APP_ZONE);

        TripSchedules tripSchedule = new TripSchedules();
        tripSchedule.setRoute(route);
        tripSchedule.setVehicle(vehicle);
        tripSchedule.setDepartureTime(request.departureTime());
        tripSchedule.setStartDate(request.startDate());
        tripSchedule.setEndDate(request.endDate());
        tripSchedule.setStatus(request.status());
        tripSchedule.setCreatedAt(now);
        tripSchedule.setUpdatedAt(now);

        return toTripScheduleResponse(tripScheduleRepository.save(tripSchedule));
    }

    @Override
    @Transactional
    public AdminTripScheduleResponse updateTripSchedule(Integer scheduleId, AdminCreateTripScheduleRequest request) {
        validateTripScheduleRequest(request);

        TripSchedules tripSchedule = tripScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay lich chay voi id = " + scheduleId));
        Routes route = routesRepository.findById(request.routeId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay tuyen voi id = " + request.routeId()));
        Vehicles vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay xe voi id = " + request.vehicleId()));

        validateVehicleCanRunRoute(vehicle);

        tripSchedule.setRoute(route);
        tripSchedule.setVehicle(vehicle);
        tripSchedule.setDepartureTime(request.departureTime());
        tripSchedule.setStartDate(request.startDate());
        tripSchedule.setEndDate(request.endDate());
        tripSchedule.setStatus(request.status());
        tripSchedule.setUpdatedAt(OffsetDateTime.now(APP_ZONE));

        return toTripScheduleResponse(tripScheduleRepository.save(tripSchedule));
    }

    @Override
    @Transactional
    public void deleteTripSchedule(Integer scheduleId) {
        if (!tripScheduleRepository.existsById(scheduleId)) {
            throw new java.util.NoSuchElementException("Khong tim thay lich chay voi id = " + scheduleId);
        }

        tripScheduleRepository.deleteById(scheduleId);
    }

    @Override
    @Transactional
    public AdminGeneratedTripsResponse generateTripsFromSchedules(AdminGenerateTripsRequest request) {
        if (request.fromDate().isAfter(request.toDate())) {
            throw new IllegalArgumentException("fromDate khong duoc sau toDate");
        }

        List<TripSchedules> schedules = resolveSchedulesForGeneration(request);
        List<AdminGeneratedTripsResponse.GeneratedTripItem> createdTrips = new ArrayList<>();
        List<String> skippedReasons = new ArrayList<>();
        Set<String> reservedVehicleSlots = new HashSet<>();
        int skippedCount = 0;

        for (TripSchedules schedule : schedules) {
            for (LocalDate date = request.fromDate(); !date.isAfter(request.toDate()); date = date.plusDays(1)) {
                if (!isScheduleApplicableOnDate(schedule, date)) {
                    continue;
                }

                OffsetDateTime departureTime = date.atTime(schedule.getDepartureTime())
                        .atZone(APP_ZONE)
                        .toOffsetDateTime();

                if (hasVehicleDepartureConflict(
                        schedule.getVehicle().getId(),
                        departureTime,
                        reservedVehicleSlots
                )) {
                    skippedCount++;
                    skippedReasons.add("Bo qua lich " + schedule.getId()
                            + " ngay " + date
                            + " vi xe " + schedule.getVehicle().getLicensePlate()
                            + " da co chuyen trung gio khoi hanh.");
                    continue;
                }

                List<SeatTemplates> seatTemplates = seatTemplateRepository
                        .findAllByVehicleTypeIdOrderByDeckAscRowIndexAscColIndexAscSeatCodeAsc(
                                schedule.getVehicle().getVehicleType().getId()
                        );

                if (seatTemplates.isEmpty()) {
                    skippedCount++;
                    skippedReasons.add("Bo qua lich " + schedule.getId()
                            + " vi xe " + schedule.getVehicle().getLicensePlate()
                            + " chua co so do ghe.");
                    continue;
                }

                Trips trip = new Trips();
                trip.setRoute(schedule.getRoute());
                trip.setVehicle(schedule.getVehicle());
                trip.setDepartureTime(departureTime);
                trip.setStatus(TRIP_STATUS_SCHEDULED);
                Trips savedTrip = tripRepository.save(trip);
                reservedVehicleSlots.add(buildVehicleDepartureSlotKey(schedule.getVehicle().getId(), departureTime));

                List<TripSeats> tripSeats = seatTemplates.stream()
                        .map(template -> {
                            TripSeats tripSeat = new TripSeats();
                            tripSeat.setTrip(savedTrip);
                            tripSeat.setSeatTemplate(template);
                            tripSeat.setStatus(SEAT_STATUS_AVAILABLE);
                            return tripSeat;
                        })
                        .toList();
                tripSeatRepository.saveAll(tripSeats);

                createdTrips.add(new AdminGeneratedTripsResponse.GeneratedTripItem(
                        schedule.getId(),
                        savedTrip.getId(),
                        buildRouteName(
                                schedule.getRoute().getOrigin().getName(),
                                schedule.getRoute().getDestination().getName()
                        ),
                        buildVehicleLabel(schedule.getVehicle()),
                        departureTime,
                        tripSeats.size()
                ));
            }
        }

        return new AdminGeneratedTripsResponse(
                request.fromDate(),
                request.toDate(),
                schedules.size(),
                createdTrips.size(),
                skippedCount,
                createdTrips,
                skippedReasons
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRoutesResponse getRoutes() {
        LocalDate today = LocalDate.now(APP_ZONE);
        OffsetDateTime start = startOfDay(today.minusDays(6));
        OffsetDateTime end = startOfDay(today.plusDays(1));

        List<RoutesRepository.AdminRouteProjection> routes = routesRepository.findAdminRoutes();
        List<TripRepository.AdminTripProjection> weekTrips = tripRepository.findAdminTripsByDepartureTimeBetween(start, end);
        Map<Integer, Long> weekTicketCounts = getTicketCounts(weekTrips);
        Map<Integer, RouteAggregate> aggregates = buildRouteAggregates(
                weekTrips,
                weekTicketCounts,
                ticketRepository.findRouteTicketDetailsByDepartureTimeBetween(start, end)
        );

        List<AdminRoutesResponse.RouteCatalogItem> routeItems = routes.stream()
                .map(route -> {
                    RouteAggregate aggregate = aggregates.get(route.getRouteId());
                    long tripCount = aggregate == null ? 0L : aggregate.tripCount();
                    BigDecimal revenue = aggregate == null ? BigDecimal.ZERO : aggregate.revenue();
                    double occupancy = aggregate == null ? 0D : aggregate.occupancyRate();
                    return new AdminRoutesResponse.RouteCatalogItem(
                            route.getRouteId(),
                            buildRouteName(route.getOriginName(), route.getDestinationName()),
                            route.getDistanceKm() == null ? 0D : route.getDistanceKm(),
                            route.getEstimatedDurationMinutes(),
                            tripCount / 7D,
                            revenue.divide(BigDecimal.valueOf(7L), 0, RoundingMode.HALF_UP),
                            occupancy
                    );
                })
                .toList();

        List<AdminRoutesResponse.RoutePriorityItem> highlights = aggregates.values().stream()
                .sorted(Comparator
                        .comparingLong(RouteAggregate::tickets).reversed()
                        .thenComparing(RouteAggregate::revenue, Comparator.reverseOrder()))
                .limit(4)
                .map(aggregate -> new AdminRoutesResponse.RoutePriorityItem(
                        aggregate.routeId(),
                        aggregate.name(),
                        aggregate.tickets(),
                        aggregate.revenue(),
                        aggregate.occupancyRate()
                ))
                .toList();

        return new AdminRoutesResponse(routeItems, highlights);
    }

    @Override
    @Transactional
    public AdminRouteDetailResponse createRoute(AdminCreateRouteRequest request) {
        validateCreateRouteRequest(request);

        Map<Integer, Locations> locationsById = loadLocationsForRouteRequest(request);

        Routes route = new Routes();
        Routes savedRoute = routesRepository.save(route);
        return persistRouteShape(savedRoute, request, locationsById);
    }

    @Override
    @Transactional
    public AdminRouteDetailResponse updateRoute(Integer routeId, AdminCreateRouteRequest request) {
        validateCreateRouteRequest(request);
        assertRouteCanBeMutated(routeId);

        Routes route = routesRepository.findById(routeId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay tuyen voi id = " + routeId));
        Map<Integer, Locations> locationsById = loadLocationsForRouteRequest(request);

        routeSegmentPriceRepository.deleteAllByRouteId(routeId);
        routeStopRepository.deleteAllByRouteId(routeId);

        return persistRouteShape(route, request, locationsById);
    }

    @Override
    @Transactional
    public void deleteRoute(Integer routeId) {
        assertRouteCanBeMutated(routeId);

        Routes route = routesRepository.findById(routeId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay tuyen voi id = " + routeId));

        routeSegmentPriceRepository.deleteAllByRouteId(routeId);
        routeStopRepository.deleteAllByRouteId(routeId);
        routesRepository.delete(route);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminRouteDetailResponse getRouteDetail(Integer routeId) {
        Routes route = routesRepository.findById(routeId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay tuyen voi id = " + routeId));

        List<RouteStops> stops = routeStopRepository.findAllByRouteIdOrderByStopOrderAsc(routeId);
        List<RouteSegmentPrices> prices = routeSegmentPriceRepository
                .findAllByRouteIdOrderByPickupStopStopOrderAscDropoffStopStopOrderAsc(routeId);

        return toRouteDetailResponse(route, stops, prices);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminFleetResponse getFleet() {
        List<VehicleRepository.AdminVehicleProjection> vehicles = vehicleRepository.findAdminVehicles();
        Set<Integer> vehicleIds = vehicles.stream()
                .map(VehicleRepository.AdminVehicleProjection::getVehicleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, OffsetDateTime> latestTripTimes = vehicleIds.isEmpty()
                ? Map.of()
                : tripRepository.findLatestTripTimesByVehicleIds(vehicleIds).stream()
                .collect(Collectors.toMap(
                        TripRepository.VehicleLastTripProjection::getVehicleId,
                        TripRepository.VehicleLastTripProjection::getLatestDepartureTime
                ));

        long activeVehicles = vehicles.stream()
                .filter(vehicle -> isActiveVehicleStatus(vehicle.getStatus()))
                .count();
        long maintenanceVehicles = vehicles.stream()
                .filter(vehicle -> isMaintenanceStatus(vehicle.getStatus()))
                .count();
        long reserveVehicles = vehicles.stream()
                .filter(vehicle -> isReserveStatus(vehicle.getStatus()))
                .count();

        List<AdminFleetResponse.SummaryItem> summary = List.of(
                new AdminFleetResponse.SummaryItem(
                        "Tong so xe",
                        Integer.toString(vehicles.size()),
                        activeVehicles + " xe co the dua vao khai thac ngay"
                ),
                new AdminFleetResponse.SummaryItem(
                        "Bao tri/dang sua",
                        Long.toString(maintenanceVehicles),
                        "Can theo doi cac xe co trang thai bao tri"
                ),
                new AdminFleetResponse.SummaryItem(
                        "Xe du phong",
                        Long.toString(reserveVehicles),
                        reserveVehicles == 0 ? "Chua co xe du phong duoc danh dau" : "San sang dieu dong khi can"
                )
        );

        List<AdminFleetResponse.VehicleItem> vehicleItems = vehicles.stream()
                .map(vehicle -> toFleetVehicleItem(vehicle, latestTripTimes.get(vehicle.getVehicleId())))
                .toList();

        return new AdminFleetResponse(
                summary,
                vehicleItems,
                buildVehicleTypeOptions(),
                buildVehicleStatusOptions()
        );
    }

    @Override
    @Transactional
    public AdminFleetResponse.VehicleItem createVehicle(AdminUpsertVehicleRequest request) {
        VehicleTypes vehicleType = vehicleTypeRepository.findById(request.vehicleTypeId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay loai xe voi id = " + request.vehicleTypeId()));

        String licensePlate = sanitizeLicensePlate(request.licensePlate());
        ensureVehicleLicensePlateAvailable(licensePlate, null);

        Vehicles vehicle = new Vehicles();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setBrand(sanitizeNullableText(request.brand()));
        vehicle.setManufactureYear(request.manufactureYear());
        vehicle.setVehicleType(vehicleType);
        vehicle.setStatus(normalizeVehicleStatus(request.status()));

        Vehicles savedVehicle = vehicleRepository.save(vehicle);
        return getFleetVehicleItem(savedVehicle.getId());
    }

    @Override
    @Transactional
    public AdminFleetResponse.VehicleItem updateVehicle(Integer vehicleId, AdminUpsertVehicleRequest request) {
        Vehicles vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay xe voi id = " + vehicleId));
        VehicleTypes vehicleType = vehicleTypeRepository.findById(request.vehicleTypeId())
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay loai xe voi id = " + request.vehicleTypeId()));

        String licensePlate = sanitizeLicensePlate(request.licensePlate());
        ensureVehicleLicensePlateAvailable(licensePlate, vehicleId);

        vehicle.setLicensePlate(licensePlate);
        vehicle.setBrand(sanitizeNullableText(request.brand()));
        vehicle.setManufactureYear(request.manufactureYear());
        vehicle.setVehicleType(vehicleType);
        vehicle.setStatus(normalizeVehicleStatus(request.status()));

        vehicleRepository.save(vehicle);
        return getFleetVehicleItem(vehicleId);
    }

    @Override
    @Transactional
    public AdminFleetResponse.VehicleItem updateVehicleStatus(Integer vehicleId, AdminUpdateVehicleStatusRequest request) {
        Vehicles vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new java.util.NoSuchElementException("Khong tim thay xe voi id = " + vehicleId));

        vehicle.setStatus(normalizeVehicleStatus(request.status()));
        vehicleRepository.save(vehicle);
        return getFleetVehicleItem(vehicleId);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStaffResponse getStaff() {
        List<AdminStaffResponse.StaffMember> members = usersRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toStaffMember)
                .toList();

        return new AdminStaffResponse(
                members,
                "He thong hien chua co bang phan ca rieng, nen man hinh nhan su dang hien thi du lieu nguoi dung va vai tro thuc te."
        );
    }

    private void validateTripScheduleRequest(AdminCreateTripScheduleRequest request) {
        if (!Set.of(SCHEDULE_STATUS_INACTIVE, SCHEDULE_STATUS_ACTIVE).contains(request.status())) {
            throw new IllegalArgumentException("Status lich chay chi nhan 0 hoac 1");
        }

        if (request.endDate() != null && request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException("Ngay ket thuc khong duoc truoc ngay bat dau");
        }
    }

    private void validateVehicleCanRunRoute(Vehicles vehicle) {
        if (vehicle.getVehicleType() == null) {
            throw new IllegalArgumentException("Xe chua duoc cau hinh loai xe");
        }
    }

    private Map<Integer, Locations> loadLocationsForRouteRequest(AdminCreateRouteRequest request) {
        Map<Integer, Locations> locationsById = locationsRepository.findAllById(
                        request.stops().stream()
                                .map(AdminCreateRouteRequest.StopRequest::locationId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(Locations::getId, location -> location));

        if (locationsById.size() != request.stops().stream().map(AdminCreateRouteRequest.StopRequest::locationId).distinct().count()) {
            throw new IllegalArgumentException("Co diem dung khong ton tai trong he thong");
        }

        return locationsById;
    }

    private AdminRouteDetailResponse persistRouteShape(
            Routes route,
            AdminCreateRouteRequest request,
            Map<Integer, Locations> locationsById
    ) {
        route.setOrigin(locationsById.get(request.stops().getFirst().locationId()));
        route.setDestination(locationsById.get(request.stops().getLast().locationId()));
        route.setDistanceKm(resolveRouteDistance(request));
        route.setEstimatedDurationMinutes(resolveRouteDuration(request));
        Routes savedRoute = routesRepository.save(route);

        List<RouteStops> savedStops = new ArrayList<>();
        Map<Integer, RouteStops> stopByOrder = new LinkedHashMap<>();

        for (int index = 0; index < request.stops().size(); index++) {
            AdminCreateRouteRequest.StopRequest stopRequest = request.stops().get(index);

            RouteStops stop = new RouteStops();
            stop.setRoute(savedRoute);
            stop.setLocation(locationsById.get(stopRequest.locationId()));
            stop.setStopOrder(index + 1);
            stop.setDistanceFromStartKm(stopRequest.distanceFromStartKm());
            stop.setEstimatedTimeFromStartMinutes(stopRequest.estimatedTimeFromStartMinutes());

            RouteStops savedStop = routeStopRepository.save(stop);
            savedStops.add(savedStop);
            stopByOrder.put(savedStop.getStopOrder(), savedStop);
        }

        List<RouteSegmentPrices> savedSegmentPrices = request.segmentPrices().stream()
                .map(segmentRequest -> {
                    RouteStops pickupStop = stopByOrder.get(segmentRequest.pickupStopOrder());
                    RouteStops dropoffStop = stopByOrder.get(segmentRequest.dropoffStopOrder());

                    RouteSegmentPrices price = new RouteSegmentPrices();
                    price.setRoute(savedRoute);
                    price.setPickupStop(pickupStop);
                    price.setDropoffStop(dropoffStop);
                    price.setPrice(segmentRequest.price());
                    return price;
                })
                .toList();

        List<RouteSegmentPrices> persistedPrices = routeSegmentPriceRepository.saveAll(savedSegmentPrices);
        return toRouteDetailResponse(savedRoute, savedStops, persistedPrices);
    }

    private void assertRouteCanBeMutated(Integer routeId) {
        if (tripScheduleRepository.existsByRouteId(routeId)) {
            throw new IllegalArgumentException("Tuyen da duoc gan vao lich chay, khong the sua hoac xoa");
        }
        if (tripRepository.existsByRouteId(routeId)) {
            throw new IllegalArgumentException("Tuyen da phat sinh chuyen xe, khong the sua hoac xoa");
        }
    }

    private void ensureVehicleLicensePlateAvailable(String licensePlate, Integer currentVehicleId) {
        boolean exists = currentVehicleId == null
                ? vehicleRepository.existsByLicensePlateIgnoreCase(licensePlate)
                : vehicleRepository.existsByLicensePlateIgnoreCaseAndIdNot(licensePlate, currentVehicleId);
        if (exists) {
            throw new IllegalArgumentException("Bien so xe da ton tai trong he thong");
        }
    }

    private List<TripSchedules> resolveSchedulesForGeneration(AdminGenerateTripsRequest request) {
        Collection<Integer> requestedIds = request.scheduleIds() == null ? List.of() : request.scheduleIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<TripSchedules> schedules = requestedIds.isEmpty()
                ? tripScheduleRepository.findActiveSchedulesForDateRange(request.fromDate(), request.toDate())
                : tripScheduleRepository.findAllByIdInOrderByCreatedAtDescIdDesc(new ArrayList<>(requestedIds));

        if (schedules.isEmpty()) {
            return List.of();
        }

        if (!requestedIds.isEmpty()) {
            Set<Integer> foundIds = schedules.stream()
                    .map(TripSchedules::getId)
                    .collect(Collectors.toSet());
            List<Integer> missingIds = requestedIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();
            if (!missingIds.isEmpty()) {
                throw new java.util.NoSuchElementException("Khong tim thay lich chay voi id: " + missingIds);
            }
        }

        return schedules;
    }

    private boolean isScheduleApplicableOnDate(TripSchedules schedule, LocalDate date) {
        if (!Objects.equals(schedule.getStatus(), SCHEDULE_STATUS_ACTIVE)) {
            return false;
        }

        if (date.isBefore(schedule.getStartDate())) {
            return false;
        }

        return schedule.getEndDate() == null || !date.isAfter(schedule.getEndDate());
    }

    private boolean hasVehicleDepartureConflict(Integer vehicleId, OffsetDateTime departureTime, Set<String> reservedVehicleSlots) {
        String slotKey = buildVehicleDepartureSlotKey(vehicleId, departureTime);
        return reservedVehicleSlots.contains(slotKey)
                || tripRepository.existsByVehicleIdAndDepartureTime(vehicleId, departureTime);
    }

    private String buildVehicleDepartureSlotKey(Integer vehicleId, OffsetDateTime departureTime) {
        return vehicleId + "|" + departureTime.toInstant();
    }

    private AdminTripScheduleResponse toTripScheduleResponse(TripSchedules schedule) {
        return new AdminTripScheduleResponse(
                schedule.getId(),
                schedule.getRoute().getId(),
                buildRouteName(
                        schedule.getRoute().getOrigin().getName(),
                        schedule.getRoute().getDestination().getName()
                ),
                schedule.getVehicle().getId(),
                buildVehicleLabel(schedule.getVehicle()),
                schedule.getDepartureTime(),
                schedule.getStartDate(),
                schedule.getEndDate(),
                schedule.getStatus(),
                mapScheduleStatus(schedule.getStatus()),
                schedule.getCreatedAt(),
                schedule.getUpdatedAt()
        );
    }

    private AdminFleetResponse.VehicleItem getFleetVehicleItem(Integer vehicleId) {
        VehicleRepository.AdminVehicleProjection vehicle = vehicleRepository.findAdminVehicleById(vehicleId);
        if (vehicle == null) {
            throw new java.util.NoSuchElementException("Khong tim thay xe voi id = " + vehicleId);
        }

        OffsetDateTime lastTrip = tripRepository.findLatestTripTimesByVehicleIds(Set.of(vehicleId)).stream()
                .findFirst()
                .map(TripRepository.VehicleLastTripProjection::getLatestDepartureTime)
                .orElse(null);

        return toFleetVehicleItem(vehicle, lastTrip);
    }

    private AdminFleetResponse.VehicleItem toFleetVehicleItem(
            VehicleRepository.AdminVehicleProjection vehicle,
            OffsetDateTime lastTrip
    ) {
        String rawStatus = normalizeVehicleStatusForResponse(vehicle.getStatus());
        return new AdminFleetResponse.VehicleItem(
                vehicle.getVehicleId(),
                defaultString(vehicle.getLicensePlate(), "CHUA-CO-BIEN-SO"),
                defaultString(vehicle.getVehicleTypeName(), "Chua cau hinh loai xe"),
                vehicle.getVehicleTypeId(),
                prettifyVehicleStatus(rawStatus),
                rawStatus,
                "Hoat dong gan nhat",
                lastTrip == null ? "Chua co du lieu chuyen" : lastTrip.atZoneSameInstant(APP_ZONE).format(DATE_TIME_FORMATTER),
                defaultString(vehicle.getBrand(), "Chua co hang xe"),
                vehicle.getManufactureYear(),
                vehicle.getTotalSeats()
        );
    }

    private List<AdminFleetResponse.VehicleTypeOption> buildVehicleTypeOptions() {
        return vehicleTypeRepository.findAllByOrderByTypeNameAscIdAsc().stream()
                .map(vehicleType -> new AdminFleetResponse.VehicleTypeOption(
                        vehicleType.getId(),
                        defaultString(vehicleType.getTypeName(), "Chua dat ten"),
                        vehicleType.getTotalSeats()
                ))
                .toList();
    }

    private List<AdminFleetResponse.StatusOption> buildVehicleStatusOptions() {
        return List.of(
                new AdminFleetResponse.StatusOption(VEHICLE_STATUS_ACTIVE, "Dang khai thac"),
                new AdminFleetResponse.StatusOption(VEHICLE_STATUS_MAINTENANCE, "Bao tri / sua chua"),
                new AdminFleetResponse.StatusOption(VEHICLE_STATUS_RESERVE, "Xe du phong")
        );
    }

    private void validateCreateRouteRequest(AdminCreateRouteRequest request) {
        if (request.stops().size() < 2) {
            throw new IllegalArgumentException("Tuyen xe phai co it nhat 2 diem dung");
        }

        Set<Integer> uniqueLocationIds = request.stops().stream()
                .map(AdminCreateRouteRequest.StopRequest::locationId)
                .collect(Collectors.toSet());

        if (uniqueLocationIds.size() != request.stops().size()) {
            throw new IllegalArgumentException("Khong duoc lap diem dung trong cung mot tuyen");
        }

        double previousDistance = -1D;
        int previousMinutes = -1;

        for (int index = 0; index < request.stops().size(); index++) {
            AdminCreateRouteRequest.StopRequest stop = request.stops().get(index);

            if (index == 0 && (stop.distanceFromStartKm() != 0D || stop.estimatedTimeFromStartMinutes() != 0)) {
                throw new IllegalArgumentException("Diem dung dau tien phai co khoang cach va thoi gian bang 0");
            }

            if (stop.distanceFromStartKm() < previousDistance) {
                throw new IllegalArgumentException("Khoang cach cua diem dung phai tang dan theo thu tu");
            }

            if (stop.estimatedTimeFromStartMinutes() < previousMinutes) {
                throw new IllegalArgumentException("Thoi gian cua diem dung phai tang dan theo thu tu");
            }

            previousDistance = stop.distanceFromStartKm();
            previousMinutes = stop.estimatedTimeFromStartMinutes();
        }

        int stopCount = request.stops().size();
        Set<String> segmentPairs = new java.util.HashSet<>();
        for (AdminCreateRouteRequest.SegmentPriceRequest segment : request.segmentPrices()) {
            if (segment.pickupStopOrder() >= segment.dropoffStopOrder()) {
                throw new IllegalArgumentException("Muc gia phai co diem don dung truoc diem tra");
            }

            if (segment.pickupStopOrder() < 1 || segment.dropoffStopOrder() > stopCount) {
                throw new IllegalArgumentException("Muc gia dang tham chieu diem dung khong hop le");
            }

            String pairKey = segment.pickupStopOrder() + "-" + segment.dropoffStopOrder();
            if (!segmentPairs.add(pairKey)) {
                throw new IllegalArgumentException("Khong duoc khai bao trung muc gia cho cung mot cap diem dung");
            }
        }
    }

    private Double resolveRouteDistance(AdminCreateRouteRequest request) {
        if (request.distanceKm() != null) {
            return request.distanceKm();
        }
        return request.stops().getLast().distanceFromStartKm();
    }

    private Integer resolveRouteDuration(AdminCreateRouteRequest request) {
        if (request.estimatedDurationMinutes() != null) {
            return request.estimatedDurationMinutes();
        }
        return request.stops().getLast().estimatedTimeFromStartMinutes();
    }

    private AdminRouteDetailResponse toRouteDetailResponse(
            Routes route,
            List<RouteStops> stops,
            List<RouteSegmentPrices> prices
    ) {
        List<AdminRouteDetailResponse.StopItem> stopItems = stops.stream()
                .sorted(Comparator.comparing(RouteStops::getStopOrder))
                .map(stop -> new AdminRouteDetailResponse.StopItem(
                        stop.getStopOrder(),
                        stop.getLocation().getId(),
                        stop.getLocation().getName(),
                        stop.getDistanceFromStartKm(),
                        stop.getEstimatedTimeFromStartMinutes()
                ))
                .toList();

        List<AdminRouteDetailResponse.SegmentPriceItem> priceItems = prices.stream()
                .sorted(Comparator
                        .comparing((RouteSegmentPrices item) -> item.getPickupStop().getStopOrder())
                        .thenComparing(item -> item.getDropoffStop().getStopOrder()))
                .map(price -> new AdminRouteDetailResponse.SegmentPriceItem(
                        price.getPickupStop().getStopOrder(),
                        price.getPickupStop().getLocation().getName(),
                        price.getDropoffStop().getStopOrder(),
                        price.getDropoffStop().getLocation().getName(),
                        price.getPrice()
                ))
                .toList();

        return new AdminRouteDetailResponse(
                route.getId(),
                buildRouteName(route.getOrigin().getName(), route.getDestination().getName()),
                route.getDistanceKm(),
                route.getEstimatedDurationMinutes(),
                stopItems,
                priceItems
        );
    }

    private OffsetDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay(APP_ZONE).toOffsetDateTime();
    }

    private Map<Integer, Long> getTicketCounts(List<TripRepository.AdminTripProjection> trips) {
        List<Integer> tripIds = trips.stream()
                .map(TripRepository.AdminTripProjection::getTripId)
                .filter(Objects::nonNull)
                .toList();

        if (tripIds.isEmpty()) {
            return Map.of();
        }

        return ticketRepository.countTicketsByTripIds(tripIds).stream()
                .collect(Collectors.toMap(
                        TicketRepository.TripTicketCountProjection::getTripId,
                        ticket -> ticket.getTicketCount() == null ? 0L : ticket.getTicketCount()
                ));
    }

    private Map<Integer, RouteAggregate> buildRouteAggregates(
            List<TripRepository.AdminTripProjection> trips,
            Map<Integer, Long> ticketCountsByTrip,
            List<TicketRepository.RouteTicketDetailProjection> routeTicketDetails
    ) {
        Map<Integer, MutableRouteAggregate> aggregateMap = new LinkedHashMap<>();

        for (TripRepository.AdminTripProjection trip : trips) {
            MutableRouteAggregate aggregate = aggregateMap.computeIfAbsent(
                    trip.getRouteId(),
                    ignored -> new MutableRouteAggregate(
                            trip.getRouteId(),
                            buildRouteName(trip.getOriginName(), trip.getDestinationName())
                    )
            );
            aggregate.tripCount++;
            aggregate.totalCapacity += safeInt(trip.getTotalSeats());
            aggregate.tickets += ticketCountsByTrip.getOrDefault(trip.getTripId(), 0L);
        }

        for (TicketRepository.RouteTicketDetailProjection ticket : routeTicketDetails) {
            MutableRouteAggregate aggregate = aggregateMap.computeIfAbsent(
                    ticket.getRouteId(),
                    ignored -> new MutableRouteAggregate(
                            ticket.getRouteId(),
                            buildRouteName(ticket.getOriginName(), ticket.getDestinationName())
                    )
            );
            if (BOOKING_STATUS_CONFIRMED == safeInt(ticket.getBookingStatus())) {
                aggregate.revenue = aggregate.revenue.add(defaultAmount(ticket.getPrice()));
            }
        }

        return aggregateMap.values().stream()
                .map(aggregate -> new RouteAggregate(
                        aggregate.routeId,
                        aggregate.name,
                        aggregate.tripCount,
                        aggregate.tickets,
                        aggregate.revenue,
                        aggregate.totalCapacity == 0 ? 0D : (double) aggregate.tickets * 100D / aggregate.totalCapacity
                ))
                .collect(Collectors.toMap(RouteAggregate::routeId, aggregate -> aggregate));
    }

    private AdminDashboardResponse.RecentBooking toRecentBooking(Bookings booking) {
        String route = "Chua xac dinh tuyen";
        String seats = "Chua co ghe";
        if (booking.getTickets() != null && !booking.getTickets().isEmpty()) {
            Tickets firstTicket = booking.getTickets().getFirst();
            if (firstTicket.getTrip() != null && firstTicket.getTrip().getRoute() != null) {
                route = buildRouteName(
                        firstTicket.getTrip().getRoute().getOrigin() == null ? null : firstTicket.getTrip().getRoute().getOrigin().getName(),
                        firstTicket.getTrip().getRoute().getDestination() == null ? null : firstTicket.getTrip().getRoute().getDestination().getName()
                );
            }

            seats = booking.getTickets().stream()
                    .map(Tickets::getTripSeat)
                    .filter(Objects::nonNull)
                    .map(tripSeat -> tripSeat.getSeatTemplate())
                    .filter(Objects::nonNull)
                    .map(seatTemplate -> seatTemplate.getSeatCode())
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.joining(", "));
            if (seats.isBlank()) {
                seats = "Chua co ghe";
            }
        }

        return new AdminDashboardResponse.RecentBooking(
                booking.getId(),
                booking.getBookingCode(),
                resolveRecentBookingCustomer(booking),
                route,
                seats,
                defaultAmount(booking.getTotalAmount()),
                mapBookingStatus(booking.getStatus()),
                booking.getBookingTime() == null ? "--:--" : booking.getBookingTime().atZoneSameInstant(APP_ZONE).format(TIME_FORMATTER)
        );
    }

    private String resolveRecentBookingCustomer(Bookings booking) {
        if (booking == null) {
            return "Khach le";
        }

        if (StringUtils.hasText(booking.getContactName()) && StringUtils.hasText(booking.getContactPhone())) {
            return booking.getContactName().trim() + " - " + booking.getContactPhone().trim();
        }

        if (StringUtils.hasText(booking.getContactName())) {
            return booking.getContactName().trim();
        }

        if (booking.getUser() != null && StringUtils.hasText(booking.getUser().getFullName())) {
            return booking.getUser().getFullName().trim();
        }

        if (StringUtils.hasText(booking.getContactPhone())) {
            return booking.getContactPhone().trim();
        }

        return "Khach le";
    }

    private List<AdminDashboardResponse.AlertItem> buildAlerts(
            long pendingBookings,
            long maintenanceVehicles,
            long reserveVehicles,
            List<AdminDashboardResponse.RouteHighlight> topRoutes
    ) {
        List<AdminDashboardResponse.AlertItem> alerts = new ArrayList<>();

        if (pendingBookings > 0) {
            alerts.add(new AdminDashboardResponse.AlertItem(
                    "Cao",
                    pendingBookings + " booking dang cho thanh toan trong hom nay",
                    "Nen doi soat de tranh het han giu ghe o cac chuyen gan gio chay."
            ));
        }

        if (maintenanceVehicles > 0) {
            alerts.add(new AdminDashboardResponse.AlertItem(
                    "Trung binh",
                    maintenanceVehicles + " xe dang o trang thai bao tri hoac can sua",
                    "Can xac nhan kha nang thay the bang xe du phong truoc khi day lich them."
            ));
        }

        if (!topRoutes.isEmpty()) {
            AdminDashboardResponse.RouteHighlight highlight = topRoutes.getFirst();
            alerts.add(new AdminDashboardResponse.AlertItem(
                    "Thong tin",
                    "Tuyen " + highlight.name() + " dang co nhu cau cao nhat",
                    "Ti le lap day hien tai dat " + formatPercent(highlight.occupancyRate()) + " trong 7 ngay gan day."
            ));
        }

        if (reserveVehicles > 0) {
            alerts.add(new AdminDashboardResponse.AlertItem(
                    "Thong tin",
                    reserveVehicles + " xe du phong da duoc danh dau san sang",
                    "Co the uu tien dieu dong neu co chuyen can tang cuong trong gio cao diem."
            ));
        }

        if (alerts.isEmpty()) {
            alerts.add(new AdminDashboardResponse.AlertItem(
                    "Thong tin",
                    "Chua ghi nhan canh bao nghiem trong",
                    "Dashboard dang cho thay he thong van hanh on dinh trong khung hien tai."
            ));
        }

        return alerts;
    }

    private List<AdminScheduleResponse.DayOption> buildScheduleDayOptions(LocalDate today, LocalDate selectedDate) {
        Set<LocalDate> dates = new LinkedHashSet<>();
        dates.add(today);
        dates.add(today.plusDays(1));
        dates.add(today.plusDays(2));
        dates.add(selectedDate);

        return dates.stream()
                .sorted()
                .map(date -> new AdminScheduleResponse.DayOption(
                        date.toString(),
                        humanDayLabel(today, date),
                        date.format(DATE_LABEL_FORMATTER)
                ))
                .toList();
    }

    private String findBusiestSlot(List<AdminScheduleResponse.TimeSlotColumn> columns) {
        return columns.stream()
                .max(Comparator.comparingInt(column -> column.trips().size()))
                .map(AdminScheduleResponse.TimeSlotColumn::slot)
                .orElse("chua xac dinh");
    }

    private AdminStaffResponse.StaffMember toStaffMember(Users user) {
        List<String> roleNames = user.getUserRoles() == null
                ? List.of()
                : user.getUserRoles().stream()
                .map(userRole -> userRole.getRole())
                .filter(Objects::nonNull)
                .map(role -> prettifyRole(role.getRoleName()))
                .distinct()
                .toList();

        String primaryRole = roleNames.isEmpty() ? "Chua gan vai tro" : String.join(", ", roleNames);

        return new AdminStaffResponse.StaffMember(
                user.getId(),
                defaultString(user.getFullName(), "Chua co ten"),
                primaryRole,
                prettifyUserStatus(user.getStatus()),
                firstNonBlank(user.getPhone(), user.getEmail(), "Chua co lien he"),
                user.getCreatedAt() == null ? "Chua ro" : user.getCreatedAt().atZoneSameInstant(APP_ZONE).format(DATE_TIME_FORMATTER),
                buildStaffFocus(roleNames)
        );
    }

    private String buildRouteName(String originName, String destinationName) {
        return defaultString(originName, "Chua ro diem di") + " - " + defaultString(destinationName, "Chua ro diem den");
    }

    private String buildVehicleLabel(Vehicles vehicle) {
        return defaultString(vehicle.getLicensePlate(), "Chua co bien so")
                + " - "
                + defaultString(vehicle.getVehicleType() == null ? null : vehicle.getVehicleType().getTypeName(), "Chua co loai xe");
    }

    private String shortDayLabel(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "T2";
            case TUESDAY -> "T3";
            case WEDNESDAY -> "T4";
            case THURSDAY -> "T5";
            case FRIDAY -> "T6";
            case SATURDAY -> "T7";
            case SUNDAY -> "CN";
        };
    }

    private String humanDayLabel(LocalDate today, LocalDate date) {
        if (date.equals(today)) {
            return "Hom nay";
        }
        if (date.equals(today.plusDays(1))) {
            return "Ngay mai";
        }

        String raw = date.getDayOfWeek().getDisplayName(TextStyle.FULL, VIETNAMESE);
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private String mapBookingStatus(Integer status) {
        if (status == null) {
            return "Chua xac dinh";
        }
        return switch (status) {
            case BOOKING_STATUS_PENDING -> "Cho thanh toan";
            case BOOKING_STATUS_CONFIRMED -> "Da thanh toan";
            default -> "Trang thai " + status;
        };
    }

    private String mapScheduleStatus(Integer status) {
        if (Objects.equals(status, SCHEDULE_STATUS_ACTIVE)) {
            return "Dang hoat dong";
        }
        if (Objects.equals(status, SCHEDULE_STATUS_INACTIVE)) {
            return "Ngung hoat dong";
        }
        return "Khong xac dinh";
    }

    private String prettifyVehicleStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Chua cap nhat";
        }
        String normalizedStatus = normalizeVehicleStatusForResponse(status);
        if (Objects.equals(normalizedStatus, VEHICLE_STATUS_ACTIVE)) {
            return "Dang khai thac";
        }
        if (Objects.equals(normalizedStatus, VEHICLE_STATUS_MAINTENANCE)) {
            return "Bao tri / sua chua";
        }
        if (Objects.equals(normalizedStatus, VEHICLE_STATUS_RESERVE)) {
            return "Xe du phong";
        }
        if (isMaintenanceStatus(status)) {
            return "Bao tri / sua chua";
        }
        if (isReserveStatus(status)) {
            return "Xe du phong";
        }
        return prettifyTitle(status);
    }

    private boolean isActiveVehicleStatus(String status) {
        return Objects.equals(normalizeVehicleStatusForResponse(status), VEHICLE_STATUS_ACTIVE);
    }

    private String prettifyUserStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Chua cap nhat";
        }
        String normalized = normalize(status);
        if (normalized.contains("ACTIVE")) {
            return "Dang hoat dong";
        }
        if (normalized.contains("LOCK") || normalized.contains("INACTIVE") || normalized.contains("DISABLE")) {
            return "Tam khoa";
        }
        return prettifyTitle(status);
    }

    private String prettifyRole(String roleName) {
        if (roleName == null || roleName.isBlank()) {
            return "Chua gan vai tro";
        }
        String normalized = normalize(roleName);
        if (normalized.contains("ADMIN")) {
            return "Quan tri";
        }
        if (normalized.contains("STAFF")) {
            return "Nhan vien";
        }
        if (normalized.contains("CUSTOMER")) {
            return "Khach hang";
        }
        if (normalized.contains("DRIVER")) {
            return "Tai xe";
        }
        return prettifyTitle(roleName);
    }

    private String buildStaffFocus(List<String> roles) {
        if (roles.stream().anyMatch(role -> role.equalsIgnoreCase("Quan tri"))) {
            return "Dieu phoi tong quan va theo doi dashboard van hanh.";
        }
        if (roles.stream().anyMatch(role -> role.equalsIgnoreCase("Tai xe"))) {
            return "Phu trach cac chuyen duoc phan cong trong he thong.";
        }
        if (roles.stream().anyMatch(role -> role.equalsIgnoreCase("Nhan vien"))) {
            return "Ho tro van hanh, xu ly booking va cap nhat thong tin chuyen.";
        }
        return "Chua co truong phan ca va mo ta cong viec rieng trong co so du lieu hien tai.";
    }

    private boolean isMaintenanceStatus(String status) {
        String normalized = normalize(status);
        return normalized.contains("MAINT")
                || normalized.contains("SERVICE")
                || normalized.contains("REPAIR")
                || normalized.contains("BAO TRI")
                || normalized.contains("SUA");
    }

    private boolean isReserveStatus(String status) {
        String normalized = normalize(status);
        return normalized.contains("RESERVE")
                || normalized.contains("STANDBY")
                || normalized.contains("DU PHONG");
    }

    private String normalizeVehicleStatus(String status) {
        String normalized = normalize(status);
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Trang thai xe khong duoc de trong");
        }
        if (normalized.contains("ACTIVE") || normalized.contains("KHAI THAC") || normalized.contains("HOAT DONG")) {
            return VEHICLE_STATUS_ACTIVE;
        }
        if (isMaintenanceStatus(normalized)) {
            return VEHICLE_STATUS_MAINTENANCE;
        }
        if (isReserveStatus(normalized)) {
            return VEHICLE_STATUS_RESERVE;
        }
        throw new IllegalArgumentException("Trang thai xe chi nhan ACTIVE, MAINTENANCE hoac RESERVE");
    }

    private String normalizeVehicleStatusForResponse(String status) {
        String normalized = normalize(status);
        if (normalized.isBlank()) {
            return VEHICLE_STATUS_ACTIVE;
        }
        if (normalized.contains("ACTIVE") || normalized.contains("KHAI THAC") || normalized.contains("HOAT DONG")) {
            return VEHICLE_STATUS_ACTIVE;
        }
        if (isMaintenanceStatus(normalized)) {
            return VEHICLE_STATUS_MAINTENANCE;
        }
        if (isReserveStatus(normalized)) {
            return VEHICLE_STATUS_RESERVE;
        }
        return status == null ? VEHICLE_STATUS_ACTIVE : status.trim().toUpperCase(Locale.ROOT);
    }

    private String sanitizeLicensePlate(String licensePlate) {
        if (licensePlate == null || licensePlate.isBlank()) {
            throw new IllegalArgumentException("Bien so xe khong duoc de trong");
        }
        return licensePlate.trim().toUpperCase(Locale.ROOT);
    }

    private String sanitizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String withoutAccents = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return withoutAccents.trim().toUpperCase(VIETNAMESE);
    }

    private String prettifyTitle(String value) {
        String cleaned = value.replace('_', ' ').trim().toLowerCase(VIETNAMESE);
        if (cleaned.isBlank()) {
            return "Chua cap nhat";
        }
        return Character.toUpperCase(cleaned.charAt(0)) + cleaned.substring(1);
    }

    private BigDecimal defaultAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }

    private String formatTime(OffsetDateTime time) {
        if (time == null) {
            return "--:--";
        }
        return time.atZoneSameInstant(APP_ZONE).format(TIME_FORMATTER);
    }

    private String formatCurrency(BigDecimal amount) {
        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString() + "d";
    }

    private String formatCurrencyCompact(BigDecimal amount) {
        BigDecimal million = BigDecimal.valueOf(1_000_000L);
        if (amount.compareTo(million) >= 0) {
            return amount.divide(million, 1, RoundingMode.HALF_UP).toPlainString() + "M";
        }
        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString() + "d";
    }

    private String formatPercent(double value) {
        return formatOneDecimal(value) + "%";
    }

    private String formatOneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String resolveUpcomingStatus(OffsetDateTime departureTime, int emptySeats) {
        if (emptySeats <= 0) {
            return "Kin cho";
        }
        if (departureTime != null && departureTime.isBefore(OffsetDateTime.now(APP_ZONE).plusMinutes(15))) {
            return "Sap khoi hanh";
        }
        if (emptySeats <= 3) {
            return "Gan kin cho";
        }
        return "Dang mo ban";
    }

    private String buildTrendCaption(
            BigDecimal todayRevenue,
            List<BookingRepository.AdminBookingMetricProjection> weekBookingMetrics
    ) {
        Map<LocalDate, BigDecimal> revenueByDate = weekBookingMetrics.stream()
                .filter(metric -> BOOKING_STATUS_CONFIRMED == safeInt(metric.getStatus()))
                .collect(Collectors.groupingBy(
                        metric -> metric.getBookingTime().atZoneSameInstant(APP_ZONE).toLocalDate(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                metric -> defaultAmount(metric.getTotalAmount()),
                                BigDecimal::add
                        )
                ));

        LocalDate today = LocalDate.now(APP_ZONE);
        BigDecimal yesterdayRevenue = revenueByDate.getOrDefault(today.minusDays(1), BigDecimal.ZERO);
        if (yesterdayRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            return "Chua co du lieu so sanh voi hom qua";
        }

        BigDecimal changePercent = todayRevenue.subtract(yesterdayRevenue)
                .multiply(BigDecimal.valueOf(100L))
                .divide(yesterdayRevenue, 1, RoundingMode.HALF_UP);

        String direction = changePercent.signum() >= 0 ? "+" : "";
        return direction + changePercent.toPlainString() + "% so voi hom qua";
    }

    private record TimeSlotDefinition(
            String label,
            String subtitle,
            LocalTime start,
            LocalTime endExclusive
    ) {
        boolean matches(OffsetDateTime departureTime) {
            if (departureTime == null) {
                return false;
            }

            LocalTime localTime = departureTime.atZoneSameInstant(AdminServiceImpl.APP_ZONE).toLocalTime();
            return !localTime.isBefore(start) && localTime.isBefore(endExclusive);
        }
    }

    private record RouteAggregate(
            Integer routeId,
            String name,
            long tripCount,
            long tickets,
            BigDecimal revenue,
            double occupancyRate
    ) {
    }

    private static final class MutableRouteAggregate {
        private final Integer routeId;
        private final String name;
        private long tripCount;
        private long tickets;
        private long totalCapacity;
        private BigDecimal revenue = BigDecimal.ZERO;

        private MutableRouteAggregate(Integer routeId, String name) {
            this.routeId = routeId;
            this.name = name;
        }
    }
}
