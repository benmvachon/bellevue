package com.village.bellevue.config.datasource;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class AsyncDataSourceConfig {

  @Bean
  @ConfigurationProperties("async.datasource")
  DataSourceProperties asyncDataSourceProperties() {
    return new DataSourceProperties();
  }

  @Bean
  @ConfigurationProperties("async.datasource.hikari")
  DataSource asyncDataSource(@Qualifier("asyncDataSourceProperties") DataSourceProperties properties) {
    return properties.initializeDataSourceBuilder().build();
  }

  @Bean
  LocalContainerEntityManagerFactoryBean asyncEntityManagerFactory(
    EntityManagerFactoryBuilder builder,
    @Qualifier("asyncDataSource") DataSource dataSource
  ) {
    return builder
        .dataSource(dataSource)
        .packages("com.village.bellevue.entity", "com.village.bellevue.entity.id", "com.village.bellevue.converter")
        .persistenceUnit("async")
        .build();
  }

  @Bean
  PlatformTransactionManager asyncTransactionManager(
    @Qualifier("asyncEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory
  ) {
    return new JpaTransactionManager(entityManagerFactory.getObject());
  }
}
