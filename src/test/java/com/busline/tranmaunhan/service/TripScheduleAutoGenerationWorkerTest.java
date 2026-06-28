package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.admin.AdminGenerateTripsRequest;
import com.busline.tranmaunhan.dto.admin.AdminGeneratedTripsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TripScheduleAutoGenerationWorkerTest {

    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private AdminService adminService;
    private TripScheduleAutoGenerationWorker worker;

    @BeforeEach
    void setUp() {
        adminService = mock(AdminService.class);
        worker = new TripScheduleAutoGenerationWorker(adminService);
    }

    @Test
    void shouldGenerateTripsForConfiguredSevenDayWindow() {
        ReflectionTestUtils.setField(worker, "autoGenerateEnabled", true);
        ReflectionTestUtils.setField(worker, "autoGenerateWindowDays", 7);
        when(adminService.generateTripsFromSchedules(any()))
                .thenReturn(new AdminGeneratedTripsResponse(
                        LocalDate.now(APP_ZONE),
                        LocalDate.now(APP_ZONE).plusDays(6),
                        0,
                        0,
                        0,
                        List.of(),
                        List.of()
                ));

        worker.generateTripsForConfiguredWindow();

        ArgumentCaptor<AdminGenerateTripsRequest> captor = ArgumentCaptor.forClass(AdminGenerateTripsRequest.class);
        verify(adminService).generateTripsFromSchedules(captor.capture());

        AdminGenerateTripsRequest request = captor.getValue();
        LocalDate today = LocalDate.now(APP_ZONE);
        assertEquals(today, request.fromDate());
        assertEquals(today.plusDays(6), request.toDate());
        assertEquals(List.of(), request.scheduleIds());
    }

    @Test
    void shouldNotGenerateTripsWhenDisabled() {
        ReflectionTestUtils.setField(worker, "autoGenerateEnabled", false);

        worker.generateTripsForConfiguredWindow();

        verify(adminService, never()).generateTripsFromSchedules(any());
    }
}
