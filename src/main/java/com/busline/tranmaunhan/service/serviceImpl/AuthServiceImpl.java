package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.config.GoogleOAuthProperties;
import com.busline.tranmaunhan.dto.auth.AuthResponse;
import com.busline.tranmaunhan.dto.auth.ChangePasswordRequest;
import com.busline.tranmaunhan.dto.auth.GoogleAuthConfigResponse;
import com.busline.tranmaunhan.dto.auth.GoogleAuthRequest;
import com.busline.tranmaunhan.dto.auth.LoginRequest;
import com.busline.tranmaunhan.dto.auth.MessageResponse;
import com.busline.tranmaunhan.dto.auth.RegisterRequest;
import com.busline.tranmaunhan.dto.auth.UserProfileResponse;
import com.busline.tranmaunhan.entity.Roles;
import com.busline.tranmaunhan.entity.UserRoles;
import com.busline.tranmaunhan.entity.UserRolesId;
import com.busline.tranmaunhan.entity.Users;
import com.busline.tranmaunhan.repository.RolesRepository;
import com.busline.tranmaunhan.repository.UserRolesRepository;
import com.busline.tranmaunhan.repository.UsersRepository;
import com.busline.tranmaunhan.security.CustomUserDetails;
import com.busline.tranmaunhan.security.JwtTokenProvider;
import com.busline.tranmaunhan.service.AuthService;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleOAuthProperties googleOAuthProperties;

    private final RestClient restClient = RestClient.builder().build();

    @Value("${app.security.default-role}")
    private String defaultRole;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usersRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        Users user = new Users();

        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setPhone(request.phone().trim());
        user.setStatus("ACTIVE");
        OffsetDateTime now = OffsetDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        Users savedUser = usersRepository.save(user);

        Roles role = rolesRepository.findByRoleNameIgnoreCase(defaultRole)
                .orElseThrow(() -> new IllegalStateException("Default role not found: " + defaultRole));

        UserRoles userRole = new UserRoles();
        userRole.setId(new UserRolesId(savedUser.getId(), role.getId()));
        userRole.setUser(savedUser);
        userRole.setRole(role);
        userRolesRepository.save(userRole);

        savedUser.setUserRoles(List.of(userRole));
        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        return buildAuthResponse(userDetails);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            String email = request.email().trim();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return buildAuthResponse(userDetails);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Email hoặc mật khẩu không hợp lệ");
        }
    }

    @Override
    public GoogleAuthConfigResponse getGoogleAuthConfig() {
        if (!googleOAuthProperties.isEnabled()) {
            return new GoogleAuthConfigResponse(false, null, null);
        }

        return new GoogleAuthConfigResponse(
                true,
                googleOAuthProperties.getClientId().trim(),
                googleOAuthProperties.getRedirectUri().trim());
    }

    @Override
    @Transactional
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        validateGoogleConfiguration();

        GoogleTokenResponse tokenResponse = exchangeGoogleCodeForToken(request.code().trim());
        GoogleUserInfoResponse googleUser = fetchGoogleUserInfo(tokenResponse.accessToken());

        if (!Boolean.TRUE.equals(googleUser.emailVerified())) {
            throw new IllegalArgumentException("Tai khoan Google chua xac minh email");
        }

        String email = normalizeRequiredValue(googleUser.email(), "Khong lay duoc email tu Google");
        Users user = usersRepository.findByEmailIgnoreCase(email)
                .orElseGet(() -> createGoogleUser(email, googleUser.name()));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        validateUserStatus(userDetails);
        return buildAuthResponse(userDetails);
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(CustomUserDetails currentUser) {
        return toUserProfile(currentUser);
    }

    @Override
    @Transactional
    public MessageResponse changePassword(ChangePasswordRequest request, Integer userId) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Khong tim thay thong tin nguoi dung"));

        String currentPassword = request.currentPassword();
        String newPassword = request.newPassword();
        String confirmNewPassword = request.confirmNewPassword();

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mat khau hien tai khong dung");
        }

        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalArgumentException("Xac nhan mat khau moi khong khop");
        }

        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mat khau moi khong duoc giong mat khau hien tai");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(OffsetDateTime.now());
        usersRepository.save(user);

        return new MessageResponse("Doi mat khau thanh cong");
    }

    private GoogleTokenResponse exchangeGoogleCodeForToken(String code) {
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", code);
        formData.add("client_id", googleOAuthProperties.getClientId().trim());
        formData.add("client_secret", googleOAuthProperties.getClientSecret().trim());
        formData.add("redirect_uri", googleOAuthProperties.getRedirectUri().trim());
        formData.add("grant_type", "authorization_code");

        try {
            GoogleTokenResponse response = restClient.post()
                    .uri("https://oauth2.googleapis.com/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(formData)
                    .retrieve()
                    .body(GoogleTokenResponse.class);

            if (response == null || !StringUtils.hasText(response.accessToken())) {
                throw new IllegalArgumentException("Google khong tra ve access token hop le");
            }

            return response;
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Khong the xac thuc voi Google. Vui long thu lai.");
        }
    }

    private GoogleUserInfoResponse fetchGoogleUserInfo(String accessToken) {
        try {
            GoogleUserInfoResponse response = restClient.get()
                    .uri("https://openidconnect.googleapis.com/v1/userinfo")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(GoogleUserInfoResponse.class);

            if (response == null) {
                throw new IllegalArgumentException("Khong lay duoc thong tin tai khoan Google");
            }

            return response;
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Khong lay duoc thong tin tai khoan Google");
        }
    }

    private Users createGoogleUser(String email, String googleName) {
        Users user = new Users();
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFullName(resolveFullName(email, googleName));
        user.setEmail(email.trim());
        user.setPhone("");
        user.setStatus("ACTIVE");
        OffsetDateTime now = OffsetDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        Users savedUser = usersRepository.save(user);
        UserRoles userRole = createDefaultRoleForUser(savedUser);
        savedUser.setUserRoles(List.of(userRole));
        return savedUser;
    }

    private UserRoles createDefaultRoleForUser(Users user) {
        Roles role = rolesRepository.findByRoleNameIgnoreCase(defaultRole)
                .orElseThrow(() -> new IllegalStateException("Default role not found: " + defaultRole));

        UserRoles userRole = new UserRoles();
        userRole.setId(new UserRolesId(user.getId(), role.getId()));
        userRole.setUser(user);
        userRole.setRole(role);
        return userRolesRepository.save(userRole);
    }

    private String resolveFullName(String email, String googleName) {
        if (StringUtils.hasText(googleName)) {
            return googleName.trim();
        }

        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String normalizeRequiredValue(String value, String message) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    private void validateGoogleConfiguration() {
        if (!googleOAuthProperties.isEnabled()) {
            throw new IllegalArgumentException("Dang nhap Google chua duoc cau hinh tren he thong");
        }
    }

    private void validateUserStatus(CustomUserDetails userDetails) {
        if (!userDetails.isAccountNonLocked()) {
            throw new IllegalArgumentException("Tai khoan da bi khoa");
        }

        if (!userDetails.isEnabled()) {
            throw new IllegalArgumentException("Tai khoan hien dang khong hoat dong");
        }
    }

    private AuthResponse buildAuthResponse(CustomUserDetails userDetails) {
        String token = jwtTokenProvider.generateToken(userDetails);
        return new AuthResponse(
                token,
                "Bearer",
                jwtTokenProvider.getJwtExpirationMs(),
                toUserProfile(userDetails));
    }

    private UserProfileResponse toUserProfile(CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();

        return new UserProfileResponse(
                userDetails.getId(),
                userDetails.getFullName(),
                userDetails.getEmail(),
                userDetails.getPhone(),
                userDetails.getStatus(),
                roles);
    }

    private record GoogleTokenResponse(
            @JsonProperty("access_token") String accessToken
    ) {
    }

    private record GoogleUserInfoResponse(
            String email,
            String name,
            @JsonProperty("email_verified") Boolean emailVerified
    ) {
    }
}
