package com.village.bellevue.config.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.Map;

public class AMBHandshakeHandler extends DefaultHandshakeHandler {
  private static final Logger log = LoggerFactory.getLogger(AMBHandshakeHandler.class);
  @Override
  protected Principal determineUser(
      ServerHttpRequest request,
      WebSocketHandler wsHandler,
      Map<String, Object> attributes
  ) {
    Long userId = fetchUserIdFromRequest(request);
    return () -> String.valueOf(userId);
  }

  private Long fetchUserIdFromRequest(ServerHttpRequest request) {
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
      Authentication authentication = (Authentication) httpServletRequest.getUserPrincipal();

      if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl userDetails) {
        log.debug("AMB Request from user: " + userDetails.getId());
        return userDetails.getId();
      }
    }
    throw new IllegalStateException("Unable to extract user ID from session.");
  }
}
