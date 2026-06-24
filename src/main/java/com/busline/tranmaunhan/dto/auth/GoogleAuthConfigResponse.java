package com.busline.tranmaunhan.dto.auth;

public record GoogleAuthConfigResponse(
        boolean enabled,
        String clientId,
        String redirectUri
) {
}
