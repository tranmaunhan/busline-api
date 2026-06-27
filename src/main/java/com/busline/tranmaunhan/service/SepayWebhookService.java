package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.config.SepayWebhookProperties;
import com.busline.tranmaunhan.dto.booking.BookingResponse;
import com.busline.tranmaunhan.dto.payment.SepayWebhookPayload;
import com.busline.tranmaunhan.dto.payment.WebhookHandlingResult;
import com.busline.tranmaunhan.dto.payment.WebhookResponse;
import com.busline.tranmaunhan.entity.Bookings;
import com.busline.tranmaunhan.repository.BookingRepository;
import com.busline.tranmaunhan.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookService {

    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("(?i)(SAIGONSTBK[A-Z0-9]+)");

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BookingRepository bookingRepository;
    private final SepayWebhookProperties properties;
    private final ObjectMapper objectMapper;
    private final BookingNotificationService bookingNotificationService;
    private final BookingResponseMapper bookingResponseMapper;

    @Transactional
    public WebhookHandlingResult handleWebhook(byte[] rawBody) {
        String body = rawBody == null ? "" : new String(rawBody, StandardCharsets.UTF_8);

        if (!StringUtils.hasText(body)) {
            return new WebhookHandlingResult(
                    HttpStatus.BAD_REQUEST,
                    WebhookResponse.error("Empty body")
            );
        }

        SepayWebhookPayload payload = parsePayload(body);
        if (payload == null || payload.id() == null) {
            return new WebhookHandlingResult(
                    HttpStatus.BAD_REQUEST,
                    WebhookResponse.error("Invalid payload")
            );
        }

        boolean inserted = persistTransaction(payload, body);
        if (!inserted) {
            return new WebhookHandlingResult(HttpStatus.OK, WebhookResponse.ok());
        }

        if (!"in".equalsIgnoreCase(payload.transferType())) {
            return new WebhookHandlingResult(HttpStatus.OK, WebhookResponse.ok());
        }

        String bookingCode = extractBookingCode(payload);
        if (!StringUtils.hasText(bookingCode)) {
            return new WebhookHandlingResult(HttpStatus.OK, WebhookResponse.ok());
        }

        long transferAmount = payload.transferAmount() == null ? 0L : payload.transferAmount();
        int updatedRows = bookingRepository.markAsPaidByBookingCodeIfPending(
                bookingCode,
                properties.getPendingBookingStatus(),
                properties.getPaidBookingStatus(),
                BigDecimal.valueOf(transferAmount)
        );

        log.info(
                "Processed SePay webhook for bookingCode={}, amount={}, updatedRows={}",
                bookingCode,
                transferAmount,
                updatedRows
        );

        if (updatedRows > 0) {
            sendConfirmedNotification(bookingCode);
        } else {
            log.warn(
                    "SePay webhook did not update booking status for bookingCode={}, amount={}",
                    bookingCode,
                    transferAmount
            );
        }

        return new WebhookHandlingResult(HttpStatus.OK, WebhookResponse.ok());
    }

    private SepayWebhookPayload parsePayload(String body) {
        try {
            return objectMapper.readValue(body, SepayWebhookPayload.class);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private boolean persistTransaction(SepayWebhookPayload payload, String body) {
        return paymentTransactionRepository.insertIfAbsent(payload, body);
    }

    private void sendConfirmedNotification(String bookingCode) {
        bookingRepository.findByBookingCodeWithDetails(bookingCode)
                .ifPresentOrElse(
                        this::notifyBookingConfirmed,
                        () -> log.warn(
                                "Cannot send confirmed notification because booking {} was not found after update",
                                bookingCode
                        )
                );
    }

    private void notifyBookingConfirmed(Bookings booking) {
        try {
            BookingResponse response = bookingResponseMapper.toBookingResponse(booking);
            bookingNotificationService.sendBookingConfirmedNotification(booking, response);
            log.info("Sent confirmed notification for bookingCode={}", booking.getBookingCode());
        } catch (Exception exception) {
            log.error("Failed to send confirmed notification for bookingCode={}", booking.getBookingCode(), exception);
        }
    }

    private String extractBookingCode(SepayWebhookPayload payload) {
        String fromCode = extractBookingCode(payload.code());
        if (StringUtils.hasText(fromCode)) {
            return fromCode;
        }

        String fromReferenceCode = extractBookingCode(payload.referenceCode());
        if (StringUtils.hasText(fromReferenceCode)) {
            return fromReferenceCode;
        }

        return extractBookingCode(payload.content());
    }

    private String extractBookingCode(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        Matcher matcher = BOOKING_CODE_PATTERN.matcher(value);
        if (!matcher.find()) {
            return null;
        }

        return matcher.group(1).toUpperCase();
    }
}
