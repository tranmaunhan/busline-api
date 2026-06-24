package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.config.SepayWebhookProperties;
import com.busline.tranmaunhan.dto.payment.SepayWebhookPayload;
import com.busline.tranmaunhan.dto.payment.WebhookHandlingResult;
import com.busline.tranmaunhan.dto.payment.WebhookResponse;
import com.busline.tranmaunhan.entity.PaymentTransaction;
import com.busline.tranmaunhan.repository.BookingRepository;
import com.busline.tranmaunhan.repository.PaymentTransactionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookService {

    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("(?i)(SAIGONSTBK[A-Z0-9]+)");
    private static final DateTimeFormatter SEPAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BookingRepository bookingRepository;
    private final SepayWebhookProperties properties;
    private final ObjectMapper objectMapper;

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
        if (paymentTransactionRepository.existsBySepayId(payload.id())) {
            return false;
        }

        long transferAmount = payload.transferAmount() == null ? 0L : payload.transferAmount();
        boolean incomingTransfer = "in".equalsIgnoreCase(payload.transferType());
        boolean outgoingTransfer = "out".equalsIgnoreCase(payload.transferType());

        PaymentTransaction transaction = PaymentTransaction.builder()
                .sepayId(payload.id())
                .gateway(defaultString(payload.gateway()))
                .transactionDate(parseTransactionDate(payload.transactionDate()))
                .accountNumber(defaultString(payload.accountNumber()))
                .subAccount(defaultString(payload.subAccount()))
                .code(defaultString(payload.code()))
                .amountIn(incomingTransfer ? transferAmount : 0L)
                .amountOut(outgoingTransfer ? transferAmount : 0L)
                .accumulated(payload.accumulated() == null ? 0L : payload.accumulated())
                .content(defaultString(payload.content()))
                .referenceCode(defaultString(payload.referenceCode()))
                .body(defaultString(body))
                .build();

        try {
            paymentTransactionRepository.saveAndFlush(transaction);
            return true;
        } catch (DataIntegrityViolationException exception) {
            log.info("Ignoring duplicated SePay webhook for sepayId={}", payload.id());
            return false;
        }
    }

    private LocalDateTime parseTransactionDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        try {
            return LocalDateTime.parse(value, SEPAY_DATE_FORMATTER);
        } catch (DateTimeParseException exception) {
            log.warn("Cannot parse SePay transactionDate={}", value);
            return null;
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

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
