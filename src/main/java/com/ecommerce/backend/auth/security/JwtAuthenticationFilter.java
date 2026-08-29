package com.ecommerce.backend.auth.security;

import com.ecommerce.backend.auth.service.JwtService;
import com.ecommerce.backend.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final UserRepository userRepository;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(HEADER);
    if (header == null || !header.startsWith(PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(PREFIX.length());
    if (jwtService.isAccessTokenValid(token)) {
      String email = jwtService.extractEmailFromAccessToken(token);
      userRepository
          .findByEmail(email)
          .ifPresent(
              user -> {
                List<SimpleGrantedAuthority> authorities =
                    user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getName()))
                        .toList();
                var authentication =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
              });
    }

    filterChain.doFilter(request, response);
  }
}
