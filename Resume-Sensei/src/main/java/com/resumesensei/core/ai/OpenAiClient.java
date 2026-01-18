package com.resumesensei.core.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAiClient {

    private final String model = System.getenv("openai.model");;
    private final String apiKey = System.getenv("OPENAI_API_KEY");

    private final OkHttpClient client;


    public OpenAiClient(OkHttpClient client) {
        this.client = client;
    }

    public String analyzeResume(String prompt) throws IOException {
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("OPENAI_API_KEY environment variable not set");
        }

        ObjectMapper mapper = new ObjectMapper();

        // Build request body as Map (SAFE)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        messages.add(Map.of(
                "role", "system",
                "content", "You are a senior technical recruiter."
        ));

        messages.add(Map.of(
                "role", "user",
                "content", prompt
        ));

        requestBody.put("messages", messages);

        String json = mapper.writeValueAsString(requestBody);

        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {

            int code = response.code();
            String responseBody = response.body() != null ? response.body().string() : "";

            System.out.println("OpenAI HTTP status: " + code);
            System.out.println("OpenAI RAW response:");
            System.out.println(responseBody);

            if (code != 200) {
                throw new RuntimeException("OpenAI API failed with status " + code + ": " + responseBody);
            }

            return responseBody;
        }
    }


}
