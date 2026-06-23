package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.dto.auth.AuthResponse;
import com.busline.tranmaunhan.dto.auth.ChangePasswordRequest;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final UserRolesRepository userRolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

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
}
