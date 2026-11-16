package com.vannchhai.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String HEADER = "Authorization";
  private static final String PREFIX = "Bearer";

  private final JwtService jwtService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String header = request.getHeader(HEADER);
    if (header == null || !header.startsWith(PREFIX)) {
      filterChain.doFilter(request, response);
    }

    String token = header.substring(PREFIX.length());

    try {
      Claims claims = jwtService.parseAndValidate(token);

      UUID userId = UUID.fromString(claims.getSubject());
      String role = claims.get("role", String.class);
      List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

      var authToken = new UsernamePasswordAuthenticationToken(userId, null, authorities);
      authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
      SecurityContextHolder.getContext().setAuthentication(authToken);

      filterChain.doFilter(request, response);
    } catch (JwtException | IllegalArgumentException ex) {
      writeUnauthorized(response, ex.getMessage());
    }
  }

  private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    Map<String, Object> body =
        Map.of(
            "type",
            "about:blank",
            "title",
            "Invalid or expired token",
            "status",
            401,
            "details",
            message == null ? "Token validation failed" : message,
            "timestamp",
            Instant.now().toString());
    response.getWriter().write(objectMapper.writeValueAsString(body));
  }
}
