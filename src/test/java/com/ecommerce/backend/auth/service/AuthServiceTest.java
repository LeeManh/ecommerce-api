package com.ecommerce.backend.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;
  @Mock private RefreshTokenService refreshTokenService;

  @InjectMocks private AuthService authService;

  @Test
  void register_shouldCreateUserWithDefaultRole_whenEmailNotTaken() {
    RegisterRequest request = new RegisterRequest("new@shop.com", "password123", "New User");
    Role userRole = Role.builder().id(1L).name("ROLE_USER").build();

    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
    when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");

    UserResponse response = authService.register(request);

    assertThat(response.email()).isEqualTo(request.email());
    assertThat(response.fullName()).isEqualTo(request.fullName());
    assertThat(response.roles()).containsExactly("ROLE_USER");
    verify(userRepository).save(any(User.class));
  }

  @Test
  void register_shouldThrow_whenEmailAlreadyExists() {
    RegisterRequest request = new RegisterRequest("exists@shop.com", "password123", "Existing");
    when(userRepository.existsByEmail(request.email())).thenReturn(true);

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

    verify(userRepository, never()).save(any());
  }

  @Test
  void register_shouldThrow_whenDefaultRoleMissing() {
    RegisterRequest request = new RegisterRequest("new@shop.com", "password123", "New User");
    when(userRepository.existsByEmail(request.email())).thenReturn(false);
    when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.register(request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.INTERNAL_ERROR);
  }

  @Test
  void login_shouldReturnTokens_whenCredentialsValid() {
    LoginRequest request = new LoginRequest("user@shop.com", "password123");
    User user =
        User.builder()
            .id(1L)
            .email(request.email())
            .password("hashed-password")
            .fullName("User")
            .roles(Set.of(Role.builder().id(1L).name("ROLE_USER").build()))
            .build();

    when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
    when(jwtService.generateAccessToken(user.getEmail())).thenReturn("access-token");
    when(jwtService.generateRefreshToken(user.getEmail())).thenReturn("refresh-token");

    AuthResponse response = authService.login(request);

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    verify(refreshTokenService).store(user.getEmail(), "refresh-token");
  }

  @Test
  void login_shouldThrow_whenUserNotFound() {
    LoginRequest request = new LoginRequest("missing@shop.com", "password123");
    when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  void login_shouldThrow_whenPasswordWrong() {
    LoginRequest request = new LoginRequest("user@shop.com", "wrong-password");
    User user =
        User.builder()
            .id(1L)
            .email(request.email())
            .password("hashed-password")
            .roles(Set.of())
            .build();

    when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
    when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> authService.login(request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

    verify(jwtService, never()).generateAccessToken(anyString());
  }

  @Test
  void logout_shouldRevokeRefreshToken() {
    authService.logout("some-refresh-token");

    verify(refreshTokenService, times(1)).revoke("some-refresh-token");
  }
}
