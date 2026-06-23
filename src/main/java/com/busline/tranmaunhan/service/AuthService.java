package com.busline.tranmaunhan.service;

import com.busline.tranmaunhan.dto.auth.AuthResponse;
import com.busline.tranmaunhan.dto.auth.ChangePasswordRequest;
import com.busline.tranmaunhan.dto.auth.LoginRequest;
import com.busline.tranmaunhan.dto.auth.MessageResponse;
import com.busline.tranmaunhan.dto.auth.RegisterRequest;
import com.busline.tranmaunhan.dto.auth.UserProfileResponse;
import com.busline.tranmaunhan.security.CustomUserDetails;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserProfileResponse getCurrentUserProfile(CustomUserDetails currentUser);

    MessageResponse changePassword(ChangePasswordRequest request, Integer userId);
}
