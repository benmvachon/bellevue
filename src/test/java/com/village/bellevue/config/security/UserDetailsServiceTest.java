package com.village.bellevue.config.security;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.repository.UserRepository;

public class UserDetailsServiceTest {

  @InjectMocks private UserDetailsServiceImpl userDetailsService;

  @Mock private UserRepository userRepository;
  @Mock private DataSource datasource;
  @Mock private CallableStatement callableStatement;
  @Mock private Connection connection;

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

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testLoadUserByUsername_UserFound() {
    when(userRepository.findByUsername("foo")).thenReturn(user);

    UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername("foo");

    assertThat(userDetails).isNotNull();
    assertThat(userDetails.getUsername()).isEqualTo(user.getUsername());
  }

  @Test
  void testLoadUserByUsername_UserNotFound() {
    when(userRepository.findByUsername("unknown")).thenReturn(null);

    Exception exception =
        org.junit.jupiter.api.Assertions.assertThrows(
            UsernameNotFoundException.class,
            () -> {
              userDetailsService.loadUserByUsername("unknown");
            });

    assertThat(exception.getMessage()).isEqualTo("User not found");
  }

  @Test
  void testCreate_UserNotAuthenticated() throws AuthorizationException, SQLException {
    try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
      mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(null);

      when(userRepository.save(user)).thenReturn(user);
      when(datasource.getConnection()).thenReturn(connection);
      when(connection.prepareCall(any())).thenReturn(callableStatement);
      when(callableStatement.getLong(any())).thenReturn(user.getId());
      when(callableStatement.getString(6)).thenReturn("cat");
      when(callableStatement.getString(7)).thenReturn("yellow_cap");

      ProfileModel createdUser = userDetailsService.create(user);

      assertThat(createdUser).isNotNull();
      assertThat(createdUser.getId()).isEqualTo(user.getId());
    }
  }

  @Test
  void testCreate_UserAuthenticated() throws AuthorizationException {
    try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
      mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(user.getId());

      ProfileModel createdUser = userDetailsService.create(user);

      assertThat(createdUser).isNull();
      verify(userRepository, never()).save(any());
    }
  }

  @Test
  void testChangePassword() {
    try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
      mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(user.getId());

      String newPassword = "newPassword";
      userDetailsService.changePassword(newPassword);

      verify(userRepository).changePassword(eq(user.getId()), anyString());
      verify(userRepository)
          .changePassword(
              eq(user.getId()),
              argThat(arg -> UserDetailsServiceImpl.passwordEncoder.matches(newPassword, arg)));
    }
  }

  @Test
  void testChangeName() {
    try (MockedStatic<SecurityConfig> mockSecurity = mockStatic(SecurityConfig.class)) {
      mockSecurity.when(SecurityConfig::getAuthenticatedUserId).thenReturn(user.getId());

      String newName = "New Foo";
      userDetailsService.changeName(newName);

      verify(userRepository).changeName(user.getId(), newName);
    }
  }
}
