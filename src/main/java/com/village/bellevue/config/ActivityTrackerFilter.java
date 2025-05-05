package com.village.bellevue.config;

import java.io.IOException;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.village.bellevue.service.ActivityService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

@Component
public class ActivityTrackerFilter extends OncePerRequestFilter {
  private final ActivityService activityService;

  public ActivityTrackerFilter(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    Long user = getAuthenticatedUserId();
    if (Objects.nonNull(user)) activityService.updateLastSeen(getAuthenticatedUserId());
    filterChain.doFilter(request, response);
  }
}

