package com.busline.tranmaunhan.dto.payment;

import org.springframework.http.HttpStatus;

public record WebhookHandlingResult(HttpStatus status, WebhookResponse response) {
}
