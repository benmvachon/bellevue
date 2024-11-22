package com.village.bellevue.integration;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.data.web.PagedModel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.village.bellevue.entity.ReviewEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;

public abstract class IntegrationTestWrapper {

    @BeforeAll
    public static void startDocker() {
        try {
            ProcessBuilder dockerProcessBuilder = new ProcessBuilder("docker-compose", "up", "-d");
            Process dockerProcess = dockerProcessBuilder.start();
            dockerProcess.waitFor(2, TimeUnit.MINUTES);

            try {
                ProcessBuilder waitForMysqlProcessBuilder = new ProcessBuilder("bash", "-c", "while [[ \"$(docker inspect -f '{{.State.Health.Status}}' mysql_db)\" != \"healthy\" ]]; do sleep 2; done");
                Process waitForMysqlProcess = waitForMysqlProcessBuilder.start();
                waitForMysqlProcess.waitFor(2, TimeUnit.MINUTES);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException("Failed to wait for MySQL", e);
            }

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

    public ObjectMapper recipeMapperWithPageSupport() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Register the custom deserializer for PagedModel
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PagedModel.class, new PagedModelDeserializer<>(SimpleRecipeEntity.class));
        mapper.registerModule(module);

        return mapper;
    }

    public ObjectMapper reviewMapperWithPageSupport() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Register the custom deserializer for PagedModel
        SimpleModule module = new SimpleModule();
        module.addDeserializer(PagedModel.class, new PagedModelDeserializer<>(ReviewEntity.class));
        mapper.registerModule(module);

        return mapper;
    }

}
