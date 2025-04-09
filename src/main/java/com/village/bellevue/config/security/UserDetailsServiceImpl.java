package com.village.bellevue.config.security;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

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

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.UserEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.repository.UserRepository;

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

  @Transactional
  public UserProfileEntity create(UserEntity user) throws AuthorizationException {
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    if (getAuthenticatedUserId() == null) {
      try (Connection connection = dataSource.getConnection();
           CallableStatement stmt = connection.prepareCall("{call add_user(?, ?, ?, ?, ?, ?)}")) {
  
        // Set input parameters (1–4)
        stmt.setString(1, user.getName());
        stmt.setString(2, user.getUsername());
        stmt.setString(3, user.getEmail());
        stmt.setBytes(4, user.getPassword().getBytes()); // Assuming password is BINARY(60)
  
        // Register output parameters (5–6)
        stmt.registerOutParameter(5, Types.INTEGER);     // p_user_id
        stmt.registerOutParameter(6, Types.VARCHAR);     // p_avatar_name
  
        stmt.executeUpdate();
  
        // Retrieve output values
        Long userId = stmt.getLong(5);
        String avatarName = stmt.getString(6);
  
        if (stmt.wasNull()) {
          throw new AuthorizationException("User creation failed — user ID is null.");
        }
  
        // Construct the user profile entity (or whatever object you use)
        user.setId(userId); // Assuming UserEntity has an ID field
        return new UserProfileEntity(user, avatarName);
  
      } catch (SQLException e) {
        throw new AuthorizationException(
          "Failed to create user. SQL command error: " + e.getMessage(), e);
      }
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
