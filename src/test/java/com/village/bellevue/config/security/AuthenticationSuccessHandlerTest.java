package com.village.bellevue.config.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.repository.ProfileRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationSuccessHandlerTest {

  @InjectMocks private AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
  @Mock private ProfileRepository profileRepository;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private Authentication authentication;
  @Mock private PrintWriter printWriter;

  private final UserEntity user =
      new UserEntity(
          1L,
          "Foo",
          "foo",
          "foo",
          "foo@foo.foo",
          false,
          new Timestamp(System.currentTimeMillis()),
          new Timestamp(System.currentTimeMillis()));
  private final UserDetailsImpl userDetails = new UserDetailsImpl(user);

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testOnAuthenticationSuccess_UserIsAuthenticated()
      throws IOException, ServletException {
    when(authentication.getPrincipal()).thenReturn(userDetails);
    when(response.getWriter()).thenReturn(printWriter);

    authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

    verify(profileRepository).setStatusOnline(user.getId());
  }

  @Test
  public void testOnAuthenticationSuccess_AuthenticationIsNull()
      throws IOException, ServletException {
    authenticationSuccessHandler.onAuthenticationSuccess(request, response, null);

    verify(profileRepository, never()).setStatusOnline(any(Long.class));
  }
}
