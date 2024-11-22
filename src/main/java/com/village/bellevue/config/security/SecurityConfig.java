package com.village.bellevue.config.security;

import static org.springframework.security.config.Customizer.withDefaults;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

@Configuration
@EnableWebSecurity
public class SecurityConfig extends GlobalAuthenticationConfigurerAdapter {

  @Autowired private UserDetailsServiceImpl userDetailsService;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
  @Autowired LogoutSuccessHandlerImpl logoutSuccessHandler;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable()) // Disable CSRF for testing; enable in production
        .authorizeHttpRequests(
            authorize ->
                authorize
                    .requestMatchers(
                        "/index.html",
                        "/api/user/login",
                        "/api/user/signup",
                        "/api/user/logout",
                        "/login",
                        "/logout",
                        "signup",
                        "/resources/**",
                        "/static/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/api/user/login")
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(customAuthenticationFailureHandler())
                    // .defaultSuccessUrl("/", true)
                    .permitAll())
        .sessionManagement(
            session ->
                session
                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
                    .maximumSessions(1)
                    .expiredUrl("/login?expired=true"))
        .logout(
            logout ->
                logout
                    .logoutUrl("/api/user/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .logoutSuccessHandler(logoutSuccessHandler)
                    .permitAll())
        .httpBasic(withDefaults());
    return http.build();
  }

  @SuppressWarnings("unused")
  @Bean
  public AuthenticationFailureHandler customAuthenticationFailureHandler() {
    return (request, response, exception) -> {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
      response.setContentType("application/json");
      response.getWriter().write("{\"error\": \"Invalid username or password\"}");
    };
  }

  @Bean
  public ServletContextInitializer servletContextInitializer(
      SessionExpirationListener sessionExpirationListener) {
    return (ServletContext servletContext) -> {
      servletContext.addListener(sessionExpirationListener);
    };
  }

  @Bean
  public AuthenticationManager authManager(HttpSecurity http) throws Exception {
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
      return userDetails.getId();
    }
    return null;
  }
}
