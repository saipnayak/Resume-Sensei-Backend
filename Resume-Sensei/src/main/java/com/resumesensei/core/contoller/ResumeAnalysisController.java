package com.resumesensei.core.contoller;

import com.resumesensei.core.dto.ResumeAnalyzeRequest;
import com.resumesensei.core.dto.ResumeAnalyzeResponse;
import com.resumesensei.core.service.ResumeAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resume")

public class ResumeAnalysisController {

    private final ResumeAnalysisService service;

    public ResumeAnalysisController(ResumeAnalysisService service) {
        this.service = service;
    }

    @PostMapping(
            value = "/analyze",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResumeAnalyzeResponse analyze(
            @RequestPart(required = false) String resumeText,
            @RequestPart(required = false) MultipartFile resumeFile
    ) throws IOException {

        String finalResumeText = resumeText;

        if ((finalResumeText == null || finalResumeText.isBlank())
                && resumeFile != null && !resumeFile.isEmpty()) {

            finalResumeText = service.extractTextFromPdf(resumeFile);
        }

        if (finalResumeText == null || finalResumeText.isBlank()) {
            throw new IllegalArgumentException("Resume text or PDF is required");
        }

        return service.analyze(finalResumeText);
    }

}
