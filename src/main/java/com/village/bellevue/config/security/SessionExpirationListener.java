package com.village.bellevue.config.security;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

import com.village.bellevue.service.ActivityService;

import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

@Component
@WebListener
public class SessionExpirationListener implements HttpSessionListener {

  private final ActivityService activityService;

  public SessionExpirationListener(ActivityService activityService) {
    this.activityService = activityService;
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    SecurityContext securityContext =
        (SecurityContext) event.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
    if (securityContext != null) {
      UserDetailsImpl userDetails =
          (UserDetailsImpl) securityContext.getAuthentication().getPrincipal();
      if (userDetails != null) {
        Long userId = userDetails.getId();
        activityService.markUserOffline(userId);
      }
    }
  }
}
