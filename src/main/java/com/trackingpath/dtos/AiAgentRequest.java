package com.trackingpath.dtos;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotBlank;

public class AiAgentRequest {

    @NotBlank(message = "Message is required")
    private String message;

    private String context;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public AiAgentRequest() {
    }

    public AiAgentRequest(String message, String context) {
        this.message = message;
        this.context = context;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    @JsonSetter("context")
    public void setContext(JsonNode context) {
        if (context == null || context.isNull()) {
            this.context = null;
            return;
        }
        if (context.isTextual()) {
            this.context = context.asText();
            return;
        }
        try {
            this.context = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(context);
        } catch (Exception ex) {
            this.context = context.toString();
        }
    }
}
