package com.busline.tranmaunhan.dto.admin;

import java.util.List;

public record AdminStaffResponse(
        List<StaffMember> staff,
        String note
) {
    public record StaffMember(
            Integer userId,
            String name,
            String role,
            String status,
            String contact,
            String joinedAt,
            String focus
    ) {
    }
}
