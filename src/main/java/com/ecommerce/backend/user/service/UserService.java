package com.ecommerce.backend.user.service;

import com.ecommerce.backend.common.exception.ApiException;
import com.ecommerce.backend.common.exception.ErrorCode;
import com.ecommerce.backend.user.dto.AdminUserResponse;
import com.ecommerce.backend.user.dto.ChangePasswordRequest;
import com.ecommerce.backend.user.dto.UserProfileUpdateRequest;
import com.ecommerce.backend.user.dto.UserResponse;
import com.ecommerce.backend.user.entity.User;
import com.ecommerce.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserResponse getProfile(User user) {
    return UserResponse.from(user);
  }

  @Transactional
  public UserResponse updateProfile(User user, UserProfileUpdateRequest request) {
    User managed = getManaged(user.getId());
    managed.setFullName(request.fullName());
    return UserResponse.from(managed);
  }

  @Transactional
  public void changePassword(User user, ChangePasswordRequest request) {
    User managed = getManaged(user.getId());

    if (!passwordEncoder.matches(request.currentPassword(), managed.getPassword())) {
      throw new ApiException(ErrorCode.INVALID_CREDENTIALS, "Current password is incorrect");
    }

    managed.setPassword(passwordEncoder.encode(request.newPassword()));
  }

  private User getManaged(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND, "User not found: " + id));
  }

  @Transactional
  public Page<AdminUserResponse> searchUsersForAdmin(
      Boolean enabled, String email, Pageable pageable) {
    return userRepository
        .findAll(UserSpecification.filter(enabled, email), pageable)
        .map(AdminUserResponse::from);
  }

  @Transactional
  public AdminUserResponse getUserForAdmin(Long userId) {
    return AdminUserResponse.from(getManaged(userId));
  }

  @Transactional
  public AdminUserResponse setUserEnabled(User currentAdmin, Long userId, boolean enabled) {
    if (!enabled && currentAdmin.getId().equals(userId)) {
      throw new ApiException(ErrorCode.CANNOT_DISABLE_SELF, "Cannot disable your own account");
    }

    User user = getManaged(userId);
    user.setEnabled(enabled);
    return AdminUserResponse.from(user);
  }
}
