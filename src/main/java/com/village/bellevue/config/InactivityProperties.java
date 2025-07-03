package com.village.bellevue.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "inactivity")
public class InactivityProperties {

  /**
   * Time (in seconds) since last activity before a user is considered idle.
   */
  private int timeout;

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
}
