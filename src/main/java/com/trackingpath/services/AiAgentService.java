package com.trackingpath.services;

import com.trackingpath.dtos.AiAgentRequest;
import com.trackingpath.dtos.AiAgentResponse;

import dev.langchain4j.model.chat.ChatModel;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
public class AiAgentService {

    private static final String MODEL_NAME = "ollama";

    private final ChatModel chatModel;

    public AiAgentService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public AiAgentResponse ask(AiAgentRequest request) {
        String answer = chatModel.chat(buildPrompt(request));
        return new AiAgentResponse(answer, MODEL_NAME, Instant.now());
    }

    private String buildPrompt(AiAgentRequest request) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are Fleet Plus AI, an assistant inside a fleet tracking and transport management system.
                Help users understand fleet operations, reports, alerts, trips, routes, devices, drivers, maintenance, and support workflows.
                Keep answers practical and concise. If the user asks for live or database-specific facts that were not provided in context, say what data is needed instead of inventing values.
                
                """);

        if (StringUtils.hasText(request.getContext())) {
            prompt.append("Fleet context:\n")
                    .append(request.getContext().trim())
                    .append("\n\n");
        }

        prompt.append("User question:\n")
                .append(request.getMessage().trim());

        return prompt.toString();
    }
}
