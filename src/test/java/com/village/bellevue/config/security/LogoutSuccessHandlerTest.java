package com.village.bellevue.config.security;

import java.io.IOException;
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
import com.village.bellevue.entity.UserEntity.AvatarType;
import com.village.bellevue.entity.UserEntity.UserStatus;
import com.village.bellevue.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogoutSuccessHandlerTest {

    @InjectMocks
    private LogoutSuccessHandlerImpl logoutSuccessHandler;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;

    private final UserEntity user = new UserEntity(1L, "Foo", "foo", "foo", UserStatus.ONLINE, AvatarType.BEE, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()));
    private final UserDetailsImpl userDetails = new UserDetailsImpl(user);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testOnLogoutSuccess_UserIsAuthenticated() throws IOException {
        when(authentication.getPrincipal()).thenReturn(userDetails);

        logoutSuccessHandler.onLogoutSuccess(request, response, authentication);

        verify(userRepository).setUserStatusOffline(user.getId());
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testOnLogoutSuccess_AuthenticationIsNull() throws IOException {
        logoutSuccessHandler.onLogoutSuccess(request, response, null);

        verify(userRepository, never()).setUserStatusOffline(any(Long.class));
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }
}
