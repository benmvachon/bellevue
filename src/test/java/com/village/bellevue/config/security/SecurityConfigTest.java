package com.village.bellevue.config.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig
class SecurityConfigTest {

    @InjectMocks
    private SecurityConfig securityConfig;
    @Mock
    public PasswordEncoder passwordEncoder;
    @Mock
    public AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
    @Mock
    public LogoutSuccessHandlerImpl logoutSuccessHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);
        when(http.authorizeHttpRequests(any())).thenReturn(http);
        when(http.formLogin(any())).thenReturn(http);
        when(http.sessionManagement(any())).thenReturn(http);
        when(http.logout(any())).thenReturn(http);
        when(http.csrf(any())).thenReturn(http);

        securityConfig.filterChain(http);

        verify(http).authorizeHttpRequests(any());
        verify(http).formLogin(any());
        verify(http).sessionManagement(any());
        verify(http).logout(any());
        verify(http).httpBasic(any());
    }

    @Test
    void testGetAuthenticatedUserId() {
        Long userId = 1L;
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockContext = mock(SecurityContext.class);
        when(userDetails.getId()).thenReturn(userId);
        when(mockAuthentication.isAuthenticated()).thenReturn(true);
        when(mockAuthentication.getPrincipal()).thenReturn(userDetails);
        when(mockContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            Long result = SecurityConfig.getAuthenticatedUserId();

            assertEquals(userId, result);
        }
    }

    @Test
    void testGetAuthenticatedUserIdWhenNotAuthenticated() {
        Long userId = 1L;
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        Authentication mockAuthentication = mock(Authentication.class);
        SecurityContext mockContext = mock(SecurityContext.class);
        when(userDetails.getId()).thenReturn(userId);
        when(mockAuthentication.isAuthenticated()).thenReturn(false);
        when(mockAuthentication.getPrincipal()).thenReturn(userDetails);
        when(mockContext.getAuthentication()).thenReturn(mockAuthentication);
        try (MockedStatic<SecurityContextHolder> mockContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockContext);
            Long result = SecurityConfig.getAuthenticatedUserId();

            assertNull(result);
        }
    }
}
