package com.village.bellevue.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

  @Bean(name = "taskExecutor")
  public AsyncTaskExecutor taskExecutor() {
    return new DelegatingSecurityContextAsyncTaskExecutor(new SimpleAsyncTaskExecutor());
  }

  @Override
  public Executor getAsyncExecutor() {
    return taskExecutor();
  }
}
