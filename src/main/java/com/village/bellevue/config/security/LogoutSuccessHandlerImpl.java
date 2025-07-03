package com.village.bellevue.config.security;

import java.io.IOException;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.village.bellevue.service.ActivityService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

  private final ActivityService activityService;

  public LogoutSuccessHandlerImpl(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void onLogoutSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    if (authentication != null) {
      HttpSession session = request.getSession(false);
      if (Objects.nonNull(session)) session.invalidate();
      request.getSession(true);
      Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
      activityService.markUserOffline(userId);
    }

    response.setStatus(HttpServletResponse.SC_OK);
  }
}
