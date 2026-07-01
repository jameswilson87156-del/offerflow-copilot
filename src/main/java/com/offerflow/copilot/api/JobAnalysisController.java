package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.DemoAnalysis;
import com.offerflow.copilot.service.DemoAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobAnalysisController {

    private final DemoAnalysisService demoAnalysisService;

    public JobAnalysisController(DemoAnalysisService demoAnalysisService) {
        this.demoAnalysisService = demoAnalysisService;
    }

    @GetMapping("/demo-analysis")
    public DemoAnalysis demoAnalysis() {
        return demoAnalysisService.getDemoAnalysis();
    }
}
