package com.trackingpath.dtos;

import java.time.Instant;

public class AiAgentResponse {

    private String answer;
    private String model;
    private Instant createdAt;

    public AiAgentResponse() {
    }

    public AiAgentResponse(String answer, String model, Instant createdAt) {
        this.answer = answer;
        this.model = model;
        this.createdAt = createdAt;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
