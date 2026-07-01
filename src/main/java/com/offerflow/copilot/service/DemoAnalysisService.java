package com.offerflow.copilot.service;

import com.offerflow.copilot.domain.DemoAnalysis;
import org.springframework.stereotype.Service;

@Service
public class DemoAnalysisService {

    private final JobIntakeService jobIntakeService;

    public DemoAnalysisService(JobIntakeService jobIntakeService) {
        this.jobIntakeService = jobIntakeService;
    }

    public DemoAnalysis getDemoAnalysis() {
        return jobIntakeService.demoAnalysis();
    }
}
