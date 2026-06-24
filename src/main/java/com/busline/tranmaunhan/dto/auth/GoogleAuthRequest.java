package com.busline.tranmaunhan.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record GoogleAuthRequest(
        @NotBlank(message = "Google authorization code is required")
        String code
) {
}
