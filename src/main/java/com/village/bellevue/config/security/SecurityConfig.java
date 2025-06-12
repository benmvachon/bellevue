package com.village.bellevue.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends GlobalAuthenticationConfigurerAdapter {
  private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

  @Value("${SPRING_APP_ALLOWED_ORIGINS}")
  private String allowedOriginsRaw;

  @Autowired private UserDetailsServiceImpl userDetailsService;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
  @Autowired LogoutSuccessHandlerImpl logoutSuccessHandler;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(authz -> authz
          .requestMatchers(
              "/index.html",
              "/api/user/login",
              "/api/user/signup",
              "/api/user/logout",
              "/login",
              "/logout",
              "/signup",
              "/resources/**",
              "/static/**")
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .formLogin(form -> form
          .loginPage("/login")
          .permitAll()
          .successHandler(authenticationSuccessHandler)
          .failureHandler(customAuthenticationFailureHandler())
      )
      .sessionManagement(session -> session
          .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
          .maximumSessions(1)
          .expiredUrl("/login?expired=true")
      )
      .logout(logout -> logout
          .logoutUrl("/api/user/logout")
          .logoutSuccessUrl("/login?logout=true")
          .logoutSuccessHandler(logoutSuccessHandler)
          .permitAll()
      );
  
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    List<String> origins = Arrays.asList(allowedOriginsRaw.split(","));
    config.setAllowedOrigins(origins);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }

  @SuppressWarnings("unused")
  @Bean
  AuthenticationFailureHandler customAuthenticationFailureHandler() {
    return (request, response, exception) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Invalid username or password\"}");
    };
  }

  @Bean
  ServletContextInitializer servletContextInitializer(
      SessionExpirationListener sessionExpirationListener) {
    return (ServletContext servletContext) -> {
      servletContext.addListener(sessionExpirationListener);
    };
  }

  @Bean
  AuthenticationManager authManager(HttpSecurity http) throws Exception {
    AuthenticationManagerBuilder authenticationManagerBuilder =
        http.getSharedObject(AuthenticationManagerBuilder.class);
    authenticationManagerBuilder
        .userDetailsService(userDetailsService)
        .passwordEncoder(passwordEncoder);
    return authenticationManagerBuilder.build();
  }

  public static Long getAuthenticatedUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null
        && authentication.isAuthenticated()
        && !"anonymousUser".equals(authentication.getPrincipal())) {
      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      log.debug("Request from user: " + userDetails.getId());
      return userDetails.getId();
    }
    return null;
  }
}
