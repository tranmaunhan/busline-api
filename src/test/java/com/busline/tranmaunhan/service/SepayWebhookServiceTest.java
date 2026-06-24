package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.config.SepayWebhookProperties;
import com.busline.tranmaunhan.dto.payment.WebhookHandlingResult;
import com.busline.tranmaunhan.repository.BookingRepository;
import com.busline.tranmaunhan.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SepayWebhookServiceTest {

    private PaymentTransactionRepository paymentTransactionRepository;
    private BookingRepository bookingRepository;
    private SepayWebhookService sepayWebhookService;

    @BeforeEach
    void setUp() {
        paymentTransactionRepository = mock(PaymentTransactionRepository.class);
        bookingRepository = mock(BookingRepository.class);

        SepayWebhookProperties properties = new SepayWebhookProperties();
        properties.setPendingBookingStatus(0);
        properties.setPaidBookingStatus(1);

        sepayWebhookService = new SepayWebhookService(
                paymentTransactionRepository,
                bookingRepository,
                properties,
                new ObjectMapper()
        );
    }

    @Test
    void shouldReturnBadRequestWhenBodyIsEmpty() {
        WebhookHandlingResult result = sepayWebhookService.handleWebhook(new byte[0]);

        assertEquals(400, result.status().value());
        assertEquals("Empty body", result.response().message());
        verify(paymentTransactionRepository, never()).insertIfAbsent(any(), any());
    }

    @Test
    void shouldIgnoreDuplicateWebhook() {
        when(paymentTransactionRepository.insertIfAbsent(any(), any())).thenReturn(false);

        WebhookHandlingResult result = sepayWebhookService.handleWebhook(validPayload(123L));

        assertEquals(200, result.status().value());
        assertTrue(result.response().success());
        verify(bookingRepository, never()).markAsPaidByBookingCodeIfPending(any(), any(), any(), any());
    }

    @Test
    void shouldMarkBookingPaidForIncomingTransfer() {
        when(paymentTransactionRepository.insertIfAbsent(any(), any())).thenReturn(true);
        when(bookingRepository.markAsPaidByBookingCodeIfPending(
                eq("SAIGONSTBK9"),
                eq(0),
                eq(1),
                eq(BigDecimal.valueOf(150000L))
        )).thenReturn(1);

        WebhookHandlingResult result = sepayWebhookService.handleWebhook(validPayload(456L));

        assertEquals(200, result.status().value());
        assertTrue(result.response().success());
        verify(bookingRepository).markAsPaidByBookingCodeIfPending(
                "SAIGONSTBK9",
                0,
                1,
                BigDecimal.valueOf(150000L)
        );
    }

    private byte[] validPayload(Long id) {
        String json = """
                {
                  "id": %d,
                  "gateway": "MB",
                  "transactionDate": "2026-06-24 15:30:00",
                  "accountNumber": "123456789",
                  "code": "Thanh toan SAIGONSTBK9",
                  "transferAmount": 150000,
                  "transferType": "in",
                  "accumulated": 500000,
                  "content": "Thanh toan don hang"
                }
                """.formatted(id);
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
