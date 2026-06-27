package com.busline.tranmaunhan.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpiredBookingCleanupWorker {

    private final ExpiredBookingCleanupService expiredBookingCleanupService;

    @Scheduled(
            cron = "${app.booking-cleanup.cron:0 * * * * *}",
            zone = "${app.booking-cleanup.zone:Asia/Ho_Chi_Minh}"
    )
    public void cleanupExpiredBookings() {
        int deletedCount = expiredBookingCleanupService.cleanupExpiredPendingBookings();
        if (deletedCount > 0) {
            log.info("BOOKING_CLEANUP removed {} expired bookings", deletedCount);
        }
    }
}
