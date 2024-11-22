package com.village.bellevue.integration;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PagedModel;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PagedModelDeserializer<T> extends JsonDeserializer<PagedModel<T>> {

    private final Class<T> contentClass;

    public PagedModelDeserializer(Class<T> contentClass) {
        this.contentClass = contentClass;
    }

    @Override
    public PagedModel<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        JsonNode node = mapper.readTree(p);

        // Extract the fields we need
        List<T> content = mapper.readValue(node.get("content").traverse(mapper), 
                                            mapper.getTypeFactory().constructCollectionType(List.class, contentClass));
        int page = node.get("page").get("number").asInt();
        int size = node.get("page").get("size").asInt();
        long totalElements = node.get("page").get("totalElements").asLong();

        // Create a PageImpl with the extracted fields
        PageImpl<T> pageImpl = new PageImpl<>(content, PageRequest.of(page, size), totalElements);

        // Return the new PagedModel instance wrapping PageImpl
        return new PagedModel<>(pageImpl);
    }
}
