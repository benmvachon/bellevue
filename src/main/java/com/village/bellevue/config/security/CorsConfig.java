package com.village.bellevue.config.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CorsConfig extends OncePerRequestFilter {
  private final List<String> allowedOrigins;

  public CorsConfig(@Value("${SPRING_APP_ALLOWED_ORIGINS}") String allowedOriginsString) {
    this.allowedOrigins = Arrays.asList(allowedOriginsString.split(","));
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  )throws ServletException, IOException {
    String origin = request.getHeader("Origin");
    if (origin != null && !allowedOrigins.contains(origin)) {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.getWriter().write("CORS origin denied");
      return;
    }

    filterChain.doFilter(request, response);
  }
}
