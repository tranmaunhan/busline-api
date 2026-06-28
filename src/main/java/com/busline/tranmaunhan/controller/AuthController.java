package com.busline.tranmaunhan.controller;

import com.busline.tranmaunhan.dto.auth.AuthResponse;
import com.busline.tranmaunhan.dto.auth.ChangePasswordRequest;
import com.busline.tranmaunhan.dto.auth.GoogleAuthConfigResponse;
import com.busline.tranmaunhan.dto.auth.GoogleAuthRequest;
import com.busline.tranmaunhan.dto.auth.LoginRequest;
import com.busline.tranmaunhan.dto.auth.MessageResponse;
import com.busline.tranmaunhan.dto.auth.RegisterRequest;
import com.busline.tranmaunhan.dto.auth.UpdateProfileRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Dang ky, dang nhap va thong tin nguoi dung")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Dang ky tai khoan", description = "Tao tai khoan moi va tra ve JWT khi thanh cong")
    @ApiResponse(responseCode = "201", description = "Dang ky thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu khong hop le", content = @Content(schema = @Schema()))
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Dang nhap", description = "Xac thuc email/password va tra ve JWT token")
    @ApiResponse(responseCode = "200", description = "Dang nhap thanh cong")
    @ApiResponse(responseCode = "401", description = "Sai thong tin dang nhap", content = @Content(schema = @Schema()))
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/google/config")
    @Operation(summary = "Lay cau hinh dang nhap Google", description = "Tra ve clientId va redirectUri cong khai de frontend khoi tao popup Google")
    @ApiResponse(responseCode = "200", description = "Lay cau hinh thanh cong")
    public ResponseEntity<GoogleAuthConfigResponse> googleConfig() {
        return ResponseEntity.ok(authService.getGoogleAuthConfig());
    }

    @PostMapping("/google")
    @Operation(summary = "Dang nhap bang Google", description = "Nhan authorization code tu Google, tao hoac dang nhap user va tra ve JWT token")
    @ApiResponse(responseCode = "200", description = "Dang nhap Google thanh cong")
    @ApiResponse(responseCode = "400", description = "Google login khong hop le", content = @Content(schema = @Schema()))
    public ResponseEntity<AuthResponse> loginWithGoogle(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.loginWithGoogle(request));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Thong tin tai khoan hien tai",
            description = "Lay thong tin user dang dang nhap",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Lay thong tin thanh cong")
    @ApiResponse(responseCode = "401", description = "Chua dang nhap", content = @Content(schema = @Schema()))
    public ResponseEntity<UserProfileResponse> me(@AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(authService.getCurrentUserProfile(currentUser));
    }

    @PutMapping("/profile")
    @Operation(
            summary = "Cap nhat thong tin tai khoan",
            description = "Cho phep user dang dang nhap cap nhat ho ten, email va so dien thoai",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Cap nhat thong tin thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu cap nhat khong hop le", content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "401", description = "Chua dang nhap", content = @Content(schema = @Schema()))
    public ResponseEntity<AuthResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(authService.updateCurrentUserProfile(request, currentUser.getId()));
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Doi mat khau",
            description = "Cho phep user dang dang nhap thay doi mat khau cua minh",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Doi mat khau thanh cong")
    @ApiResponse(responseCode = "400", description = "Du lieu khong hop le hoac mat khau hien tai sai",
            content = @Content(schema = @Schema()))
    @ApiResponse(responseCode = "401", description = "Chua dang nhap", content = @Content(schema = @Schema()))
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return ResponseEntity.ok(authService.changePassword(request, currentUser.getId()));
    }
}
