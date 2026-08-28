package com.ecommerce.backend.auth.service;

import com.ecommerce.backend.auth.dto.AuthResponse;
import com.ecommerce.backend.auth.dto.LoginRequest;
import com.ecommerce.backend.auth.dto.RegisterRequest;
import com.ecommerce.backend.auth.entity.Role;
import com.ecommerce.backend.auth.repository.RoleRepository;
import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.user.dto.UserResponse;
import com.ecommerce.backend.user.entity.User;
import com.ecommerce.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ApiException(ErrorCode.EMAIL_ALREADY_EXISTS, "Email already registered");
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR, "Default role not found"));

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .roles(Set.of(defaultRole))
                .build();

        userRepository.save(user);
        return UserResponse.from(user);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        return issueTokens(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new ApiException(ErrorCode.INVALID_TOKEN, "Refresh token is invalid or expired");
        }

        String email = refreshTokenService.getEmail(refreshToken);
        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "User not found"));

        refreshTokenService.revoke(refreshToken);
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());
        refreshTokenService.store(user.getEmail(), refreshToken);
        return new AuthResponse(accessToken, refreshToken, UserResponse.from(user));
    }
}
