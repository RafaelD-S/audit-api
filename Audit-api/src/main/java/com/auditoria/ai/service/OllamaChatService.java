package com.auditoria.ai.service;

import com.auditoria.ai.exception.AuditGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class OllamaChatService {

    private static final String MODEL_NAME = "qwen3:30b";

    private final RestClient restClient;

    public OllamaChatService(
            RestClient.Builder restClientBuilder,
            @Value("${ollama.api.base-url:https://ollama-api.ifba.edu.br}") String baseUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public String generate(String prompt) {
        Map<String, Object> request = Map.of(
                "model", MODEL_NAME,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "stream", false
        );

        try {
            var response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(Map.class);

            if (response == null
                    || !(response.get("message") instanceof Map<?, ?> message)
                    || !(message.get("content") instanceof String content)
                    || content.isBlank()) {
                throw new AuditGenerationException("O Ollama retornou uma resposta vazia ou inválida.");
            }

            return content;
        } catch (RestClientException exception) {
            throw new AuditGenerationException("Não foi possível gerar o texto usando o Ollama.", exception);
        }
    }
}
