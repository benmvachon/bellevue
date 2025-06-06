package com.village.bellevue.config.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.village.bellevue.service.ActivityService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {
  private static final Logger log = LoggerFactory.getLogger(AuthenticationSuccessHandlerImpl.class);

  private final ActivityService activityService;

  public AuthenticationSuccessHandlerImpl(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void onAuthenticationSuccess(
    HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication
  ) throws IOException, ServletException {
    if (authentication != null) {

      Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
      activityService.updateLastSeen(userId);

      log.debug("Successfully authenticated as " + userId);

      // Set response headers and send plain text
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(userId.toString());
      response.getWriter().flush();
    }
  }
}
