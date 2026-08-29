package com.ecommerce.backend.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.user.dto.AdminUserResponse;
import com.ecommerce.backend.user.dto.ChangePasswordRequest;
import com.ecommerce.backend.user.dto.UserProfileUpdateRequest;
import com.ecommerce.backend.user.dto.UserResponse;
import com.ecommerce.backend.user.entity.User;
import com.ecommerce.backend.user.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private UserService userService;

  @Test
  void getProfile_shouldReturnCurrentUserData() {
    User user =
        User.builder().id(1L).email("user@shop.com").fullName("User").roles(Set.of()).build();

    UserResponse response = userService.getProfile(user);

    assertThat(response.email()).isEqualTo("user@shop.com");
    assertThat(response.fullName()).isEqualTo("User");
  }

  @Test
  void updateProfile_shouldUpdateFullName_whenUserExists() {
    User principal = User.builder().id(1L).email("user@shop.com").roles(Set.of()).build();
    User managed =
        User.builder().id(1L).email("user@shop.com").fullName("Old Name").roles(Set.of()).build();
    UserProfileUpdateRequest request = new UserProfileUpdateRequest("New Name");

    when(userRepository.findById(1L)).thenReturn(Optional.of(managed));

    UserResponse response = userService.updateProfile(principal, request);

    assertThat(response.fullName()).isEqualTo("New Name");
    assertThat(managed.getFullName()).isEqualTo("New Name");
  }

  @Test
  void updateProfile_shouldThrow_whenUserNotFound() {
    User principal = User.builder().id(99L).email("ghost@shop.com").roles(Set.of()).build();
    UserProfileUpdateRequest request = new UserProfileUpdateRequest("Name");

    when(userRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userService.updateProfile(principal, request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  void changePassword_shouldEncodeAndSetNewPassword_whenCurrentPasswordMatches() {
    User principal = User.builder().id(1L).email("user@shop.com").roles(Set.of()).build();
    User managed =
        User.builder().id(1L).email("user@shop.com").password("hashed-old").roles(Set.of()).build();
    ChangePasswordRequest request = new ChangePasswordRequest("old-pass", "new-password123");

    when(userRepository.findById(1L)).thenReturn(Optional.of(managed));
    when(passwordEncoder.matches("old-pass", "hashed-old")).thenReturn(true);
    when(passwordEncoder.encode("new-password123")).thenReturn("hashed-new");

    userService.changePassword(principal, request);

    assertThat(managed.getPassword()).isEqualTo("hashed-new");
  }

  @Test
  void changePassword_shouldThrow_whenCurrentPasswordIncorrect() {
    User principal = User.builder().id(1L).email("user@shop.com").roles(Set.of()).build();
    User managed =
        User.builder().id(1L).email("user@shop.com").password("hashed-old").roles(Set.of()).build();
    ChangePasswordRequest request = new ChangePasswordRequest("wrong-pass", "new-password123");

    when(userRepository.findById(1L)).thenReturn(Optional.of(managed));
    when(passwordEncoder.matches("wrong-pass", "hashed-old")).thenReturn(false);

    assertThatThrownBy(() -> userService.changePassword(principal, request))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  void searchUsersForAdmin_shouldReturnMappedPage() {
    User user = User.builder().id(1L).email("user@shop.com").roles(Set.of()).build();
    Pageable pageable = Pageable.unpaged();
    Page<User> page = new PageImpl<>(List.of(user));

    when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

    Page<AdminUserResponse> result = userService.searchUsersForAdmin(true, "user", pageable);

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().get(0).email()).isEqualTo("user@shop.com");
  }

  @Test
  void getUserForAdmin_shouldReturnUser_whenExists() {
    User user = User.builder().id(1L).email("user@shop.com").roles(Set.of()).build();
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    AdminUserResponse response = userService.getUserForAdmin(1L);

    assertThat(response.id()).isEqualTo(1L);
  }

  @Test
  void setUserEnabled_shouldDisableUser_whenNotTargetingSelf() {
    User admin = User.builder().id(1L).email("admin@shop.com").roles(Set.of()).build();
    User target = User.builder().id(2L).email("user@shop.com").roles(Set.of()).build();
    when(userRepository.findById(2L)).thenReturn(Optional.of(target));

    AdminUserResponse response = userService.setUserEnabled(admin, 2L, false);

    assertThat(response.enabled()).isFalse();
    assertThat(target.isEnabled()).isFalse();
  }

  @Test
  void setUserEnabled_shouldThrow_whenAdminTriesToDisableSelf() {
    User admin = User.builder().id(1L).email("admin@shop.com").roles(Set.of()).build();

    assertThatThrownBy(() -> userService.setUserEnabled(admin, 1L, false))
        .isInstanceOf(ApiException.class)
        .extracting(ex -> ((ApiException) ex).getCode())
        .isEqualTo(ErrorCode.CANNOT_DISABLE_SELF);
  }
}
