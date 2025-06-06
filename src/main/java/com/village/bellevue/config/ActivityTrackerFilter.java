package com.village.bellevue.config;

import java.io.IOException;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger log = LoggerFactory.getLogger(ActivityTrackerFilter.class);
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
    log.debug("Updating last seen for user: " + user);
    if (Objects.nonNull(user)) activityService.updateLastSeen(user);
    filterChain.doFilter(request, response);
  }
}

