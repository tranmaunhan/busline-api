package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.payment.WebhookHandlingResult;
import com.busline.tranmaunhan.dto.payment.WebhookResponse;
import com.busline.tranmaunhan.service.SepayWebhookService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SepayWebhookController {

    private final SepayWebhookService sepayWebhookService;

    @PostMapping(
            path = {"/api/webhook/sepay", "/webhook/sepay"},
            consumes = MediaType.ALL_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<WebhookResponse> handleWebhook(
            @RequestBody(required = false) byte[] rawBody,
            @RequestHeader(name = "x-sepay-signature", required = false) String signature,
            @RequestHeader(name = "x-sepay-timestamp", required = false) String timestampHeader,
            HttpServletRequest request
    ) {
        long parsedTimestamp = parseTimestamp(timestampHeader);

        log.info(
                "SePay webhook received: method={}, path={}, clientIp={}, forwardedFor={}, hasSignature={}, signaturePreview={}, hasTimestampHeader={}, rawTimestampHeader={}, parsedTimestamp={}, contentType={}, userAgent={}, bodyBytes={}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr(),
                request.getHeader("X-Forwarded-For"),
                signature != null && !signature.isBlank(),
                maskSignature(signature),
                timestampHeader != null && !timestampHeader.isBlank(),
                timestampHeader,
                parsedTimestamp,
                request.getContentType(),
                request.getHeader("User-Agent"),
                rawBody == null ? 0 : rawBody.length
        );

        try {
            WebhookHandlingResult result = sepayWebhookService.handleWebhook(rawBody);
            return ResponseEntity.status(result.status()).body(result.response());
        } catch (Exception exception) {
            log.error("SePay webhook error", exception);
            return ResponseEntity.internalServerError().body(WebhookResponse.error("Internal error"));
        }
    }

    private long parseTimestamp(String timestampHeader) {
        if (timestampHeader == null || timestampHeader.isBlank()) {
            return 0L;
        }

        try {
            return Long.parseLong(timestampHeader);
        } catch (NumberFormatException exception) {
            log.warn("Invalid SePay timestamp header: {}", timestampHeader);
            return 0L;
        }
    }

    private String maskSignature(String signature) {
        if (signature == null || signature.isBlank()) {
            return "missing";
        }

        if (signature.length() <= 16) {
            return signature;
        }

        return signature.substring(0, 12) + "...";
    }
}
