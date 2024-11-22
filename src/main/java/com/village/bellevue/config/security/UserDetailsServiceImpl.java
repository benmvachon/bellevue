package com.village.bellevue.config.security;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.repository.UserRepository;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Configuration
public class UserDetailsServiceImpl implements UserDetailsService {

  @Autowired private UserRepository userRepository;
  @Autowired private DataSource dataSource;

  protected static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("User not found");
    }
    return new UserDetailsImpl(user);
  }

  public ScrubbedUserEntity create(UserEntity user) {
    if (getAuthenticatedUserId() == null) {
      user.setPassword(passwordEncoder.encode(user.getPassword()));
      user = userRepository.save(user);
      return new ScrubbedUserEntity(user);
    }
    return null;
  }

  @Transactional
  public void delete() throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    try (Connection connection = dataSource.getConnection();
        CallableStatement stmt = connection.prepareCall("{call delete_user(?)}")) {
      stmt.setLong(1, user);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new AuthorizationException(
          "Failed to delete user. SQL command error: " + e.getMessage(), e);
    }
  }

  public void changePassword(String password) {
    userRepository.changePassword(getAuthenticatedUserId(), passwordEncoder.encode(password));
  }

  public void changeName(String name) {
    userRepository.changeName(getAuthenticatedUserId(), name);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return passwordEncoder;
  }
}
