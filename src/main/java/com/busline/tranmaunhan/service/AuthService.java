package com.busline.tranmaunhan.service;

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

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse loginAdmin(LoginRequest request);

    GoogleAuthConfigResponse getGoogleAuthConfig();

    AuthResponse loginWithGoogle(GoogleAuthRequest request);

    UserProfileResponse getCurrentUserProfile(CustomUserDetails currentUser);

    UserProfileResponse getCurrentAdminProfile(CustomUserDetails currentUser);

    AuthResponse updateCurrentUserProfile(UpdateProfileRequest request, Integer userId);

    MessageResponse changePassword(ChangePasswordRequest request, Integer userId);
}
