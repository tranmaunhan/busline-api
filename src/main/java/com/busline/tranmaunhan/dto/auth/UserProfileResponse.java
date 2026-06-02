package com.busline.tranmaunhan.dto.auth;

import java.util.List;

public record UserProfileResponse(
        Integer id,
        String username,
        String fullName,
        String email,
        String phone,
        String status,
        List<String> roles
) {
}
