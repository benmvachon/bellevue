package com.village.bellevue.config.security;

import com.village.bellevue.repository.UserRepository;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Component;

@Component
@WebListener
public class SessionExpirationListener implements HttpSessionListener {

  @Autowired private UserRepository userRepository;

  @Override
  public void sessionDestroyed(HttpSessionEvent event) {
    SecurityContext securityContext =
        (SecurityContext) event.getSession().getAttribute("SPRING_SECURITY_CONTEXT");
    if (securityContext != null) {
      UserDetailsImpl userDetails =
          (UserDetailsImpl) securityContext.getAuthentication().getPrincipal();
      if (userDetails != null) {
        Long userId = userDetails.getId();
        userRepository.setUserStatusOffline(userId);
      }
    }
  }
}
