package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.admin.AdminGenerateTripsRequest;
import com.busline.tranmaunhan.dto.admin.AdminGeneratedTripsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripScheduleAutoGenerationWorker {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final AdminService adminService;

    @Value("${app.trip-schedule.auto-generate.enabled:true}")
    private boolean autoGenerateEnabled;

    @Scheduled(
            cron = "${app.trip-schedule.auto-generate.cron:0 0 0 * * *}",
            zone = "${app.trip-schedule.auto-generate.zone:Asia/Ho_Chi_Minh}"
    )
    public void generateTripsForToday() {
        if (!autoGenerateEnabled) {
            return;
        }

        LocalDate today = LocalDate.now(APP_ZONE);

        try {
            AdminGeneratedTripsResponse result = adminService.generateTripsFromSchedules(
                    new AdminGenerateTripsRequest(today, today, List.of())
            );

            log.info(
                    "AUTO_TRIP_GENERATION completed for date={}, schedulesProcessed={}, tripsCreated={}, tripsSkipped={}",
                    today,
                    result.schedulesProcessed(),
                    result.tripsCreated(),
                    result.tripsSkipped()
            );

            if (!result.skippedReasons().isEmpty()) {
                result.skippedReasons().forEach(reason -> log.warn("AUTO_TRIP_GENERATION skip: {}", reason));
            }
        } catch (Exception ex) {
            log.error("AUTO_TRIP_GENERATION failed for date={}", today, ex);
        }
    }
}
