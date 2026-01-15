package com.resumesensei.core.service;

import com.resumesensei.core.dto.ResumeAnalyzeResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeAnalysisService {
    public ResumeAnalyzeResponse analyze(String resumeText) {
        ResumeAnalyzeResponse response = new ResumeAnalyzeResponse();
        response.setScore(80);
        response.setStrengths(List.of("Strong Java fundamentals"));
        response.setImprovements(List.of("Add system design projects"));
        response.setInterviewQuestions(
                List.of("Explain Spring Boot auto-configuration"));

        return response;
    }
}

