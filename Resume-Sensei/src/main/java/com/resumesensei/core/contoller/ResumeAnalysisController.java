package com.resumesensei.core.contoller;

import com.resumesensei.core.dto.ResumeAnalyzeRequest;
import com.resumesensei.core.dto.ResumeAnalyzeResponse;
import com.resumesensei.core.service.ResumeAnalysisService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "http://localhost:5173")
public class ResumeAnalysisController {

    private final ResumeAnalysisService service;

    public ResumeAnalysisController(ResumeAnalysisService service) {
        this.service = service;
    }

    @PostMapping("/analyze")
    public ResumeAnalyzeResponse analyze(
            @RequestBody ResumeAnalyzeRequest request) {
        return service.analyze(request.getResumeText());
    }
}
