package com.village.bellevue.config.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.village.bellevue.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationSuccessHandlerImpl implements AuthenticationSuccessHandler {

  @Autowired private UserRepository userRepository;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {

    if (authentication != null) {
      Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
      userRepository.setUserStatusOnline(userId);

      // Set response headers and send plain text
      response.setContentType("text/plain");
      response.setCharacterEncoding("UTF-8");
      response.getWriter().write(userId.toString());
      response.getWriter().flush();
    }
  }
}
