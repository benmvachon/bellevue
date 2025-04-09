package com.village.bellevue.config.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.village.bellevue.repository.ProfileRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

  @Autowired private ProfileRepository profileRepository;

  @Override
  public void onLogoutSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {

    if (authentication != null) {
      Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
      profileRepository.setStatusOffline(userId);
    }

    response.setStatus(HttpServletResponse.SC_OK); // Or any other status you want
  }
}
