package com.resumesensei.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumesensei.core.ai.OpenAiClient;
import com.resumesensei.core.dto.ResumeAnalyzeResponse;
import org.springframework.stereotype.Service;

@Service
public class ResumeAnalysisService {

    private final OpenAiClient openAiClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public ResumeAnalysisService(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    public ResumeAnalyzeResponse analyze(String resumeText) {
        try {
            String prompt = buildPrompt(resumeText);
            String aiResponse = openAiClient.analyzeResume(prompt);

            JsonNode root = mapper.readTree(aiResponse);

            //  Handle OpenAI error object
            if (root.has("error")) {
                throw new RuntimeException("OpenAI error: " + root.get("error").toString());
            }

            String content = root
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (content == null || content.isBlank()) {
                throw new RuntimeException("Empty content from OpenAI");
            }

            String cleanedJson = extractJson(content);

            return mapper.readValue(cleanedJson, ResumeAnalyzeResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("AI analysis failed: " + e.getMessage(), e);
        }
    }

    private String extractJson(String text) {

        // Remove markdown ```json ``` if present
        text = text.replaceAll("```json", "")
                .replaceAll("```", "")
                .trim();

        // Extract JSON object boundaries
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");

        if (start == -1 || end == -1 || start > end) {
            throw new RuntimeException("No valid JSON found in AI response");
        }

        return text.substring(start, end + 1);
    }


    private String buildPrompt(String resumeText) {
        return """
        You are a senior technical recruiter.

        Analyze the following resume for a Java backend developer role.

        Return the response strictly in JSON format:
        {
          "score": number (0-100),
          "strengths": string[],
          "improvements": string[],
          "interviewQuestions": string[]
        }

        Resume:
        %s
        """.formatted(resumeText);
    }
}
