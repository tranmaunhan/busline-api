package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.auth.AuthResponse;
import com.busline.tranmaunhan.dto.auth.LoginRequest;
import com.busline.tranmaunhan.dto.auth.UserProfileResponse;
import com.busline.tranmaunhan.security.CustomUserDetails;
import com.busline.tranmaunhan.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
@Tag(name = "Admin Authentication", description = "Dang nhap va xac thuc nguoi dung admin")
public class AdminAuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Dang nhap admin", description = "Cho phep tai khoan co role ADMIN hoac STAFF dang nhap vao trang admin")
    @ApiResponse(responseCode = "200", description = "Dang nhap admin thanh cong")
    @ApiResponse(responseCode = "400", description = "Tai khoan khong co quyen admin", content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "401", description = "Sai thong tin dang nhap", content = @Content(schema = @Schema()))
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.loginAdmin(request));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Thong tin admin hien tai",
            description = "Lay thong tin tai khoan admin dang dang nhap",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Lay thong tin admin thanh cong")
    @ApiResponse(responseCode = "401", description = "Chua dang nhap", content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "403", description = "Khong co quyen admin", content = @Content(schema = @Schema()))
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(authService.getCurrentAdminProfile(currentUser));
    }
}
