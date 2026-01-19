package com.resumesensei.core.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumesensei.core.ai.OpenAiClient;
import com.resumesensei.core.dto.ResumeAnalyzeResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

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

        String cleaned = resumeText.replaceAll("\\s+", " ").trim();

        // HARD LIMIT
        if (cleaned.length() > 2500) {
            cleaned = cleaned.substring(0, 2500);
        }

        return """
    You are a senior technical recruiter.

    Analyze the resume and return ONLY valid JSON in this format:
    {
      "score": number,
      "strengths": [string],
      "improvements": [string],
      "interviewQuestions": [string]
    }

    Resume:
    %s
    """.formatted(cleaned);
    }
    public String extractTextFromPdf(MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded PDF is empty");
        }

        try (PDDocument document = PDDocument.load(file.getInputStream())) {

            if (document.isEncrypted()) {
                throw new RuntimeException("PDF is encrypted and cannot be processed");
            }

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new RuntimeException(
                        "No readable text found in PDF. The file may be scanned or image-based."
                );
            }

            return text;

        } catch (IOException e) {
            throw new RuntimeException("Failed to extract text from PDF", e);
        }
    }


}
