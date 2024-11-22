package com.village.bellevue.config.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;

public class AuthenticationSuccessHandlerTest {

  @InjectMocks private AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
  @Mock private UserRepository userRepository;
  @Mock private HttpServletRequest request;
  @Mock private HttpServletResponse response;
  @Mock private Authentication authentication;

  private final UserEntity user =
      new UserEntity(
          1L,
          "Foo",
          "foo",
          "foo",
          UserStatus.ONLINE,
          AvatarType.BEE,
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

    authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);

    verify(userRepository).setUserStatusOnline(user.getId());
  }

  @Test
  public void testOnAuthenticationSuccess_AuthenticationIsNull()
      throws IOException, ServletException {
    authenticationSuccessHandler.onAuthenticationSuccess(request, response, null);

    verify(userRepository, never()).setUserStatusOnline(any(Long.class));
  }
}
