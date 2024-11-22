package com.village.bellevue.config.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.village.bellevue.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {

        if (authentication != null) {
            Long userId = ((UserDetailsImpl) authentication.getPrincipal()).getId();
            userRepository.setUserStatusOffline(userId);
        }

        response.setStatus(HttpServletResponse.SC_OK); // Or any other status you want
    }
}
