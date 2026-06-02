package com.busline.tranmaunhan.service.serviceImpl;

import com.busline.tranmaunhan.dto.auth.AuthResponse;
import com.busline.tranmaunhan.dto.auth.LoginRequest;
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

import java.time.LocalDateTime;
import java.util.List;

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
        if (usersRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (usersRepository.existsByEmailIgnoreCase(request.email())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Users user = new Users();
        user.setUsername(request.username().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setEmail(request.email().trim());
        user.setPhone(request.phone().trim());
        user.setStatus("ACTIVE");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

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
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return buildAuthResponse(userDetails);
        } catch (org.springframework.security.core.AuthenticationException ex) {
            throw new BadCredentialsException("Invalid username or password");
        }
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(CustomUserDetails currentUser) {
        return toUserProfile(currentUser);
    }

    private AuthResponse buildAuthResponse(CustomUserDetails userDetails) {
        String token = jwtTokenProvider.generateToken(userDetails);
        return new AuthResponse(
                token,
                "Bearer",
                jwtTokenProvider.getJwtExpirationMs(),
                toUserProfile(userDetails)
        );
    }

    private UserProfileResponse toUserProfile(CustomUserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .toList();

        return new UserProfileResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getEmail(),
                userDetails.getPhone(),
                userDetails.getStatus(),
                roles
        );
    }
}
