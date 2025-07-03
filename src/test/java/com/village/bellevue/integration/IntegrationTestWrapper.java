package com.village.bellevue.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.data.web.PagedModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.RatingEntity;
import com.village.bellevue.model.PostModel;

public abstract class IntegrationTestWrapper {

  @BeforeAll
  public static void startDocker() {
    try {
      Path envPath = Paths.get(".env");
      if (Files.exists(envPath)) {
        List<String> lines = Files.readAllLines(envPath);
        for (String line : lines) {
          if (line.trim().isEmpty() || line.startsWith("#")) continue;
          String[] parts = line.split("=", 2);
          if (parts.length == 2) {
            String key = parts[0].trim();
            String value = parts[1].trim();
            System.setProperty(key, value);
          }
        }
      }

      ProcessBuilder dockerProcessBuilder = new ProcessBuilder("docker-compose", "up", "-d");
      dockerProcessBuilder.inheritIO();
      Process dockerProcess = dockerProcessBuilder.start();
      dockerProcess.waitFor(2, TimeUnit.MINUTES);

      ProcessBuilder waitForMysqlProcessBuilder =
          new ProcessBuilder(
              "bash",
              "-c",
              "while [[ \"$(docker inspect -f '{{.State.Health.Status}}' mysql_db)\" != \"healthy\" ]]; do sleep 2; done");
      Process waitForMysqlProcess = waitForMysqlProcessBuilder.start();
      waitForMysqlProcess.waitFor(2, TimeUnit.MINUTES);

    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to start Docker containers", e);
    }
  }

  @AfterAll
  public static void stopDocker() {
    try {
      ProcessBuilder builder = new ProcessBuilder("docker-compose", "down");
      Process process = builder.start();
      process.waitFor(2, TimeUnit.MINUTES);
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException("Failed to stop Docker containers", e);
    }
  }

  public ObjectMapper forumMapperWithPageSupport() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    SimpleModule module = new SimpleModule();
    module.addDeserializer(
        PagedModel.class, new PagedModelDeserializer<>(ForumEntity.class));
    mapper.registerModule(module);

    return mapper;
  }

  public ObjectMapper postMapperWithPageSupport() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    SimpleModule module = new SimpleModule();
    module.addDeserializer(
        PagedModel.class, new PagedModelDeserializer<>(PostModel.class));
    mapper.registerModule(module);

    return mapper;
  }

  public ObjectMapper ratingMapperWithPageSupport() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());

    SimpleModule module = new SimpleModule();
    module.addDeserializer(PagedModel.class, new PagedModelDeserializer<>(RatingEntity.class));
    mapper.registerModule(module);

    return mapper;
  }
}
