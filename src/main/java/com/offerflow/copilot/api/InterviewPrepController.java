package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.InterviewPrepDemo;
import com.offerflow.copilot.service.InterviewPrepService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview-prep")
public class InterviewPrepController {

    private final InterviewPrepService interviewPrepService;

    public InterviewPrepController(InterviewPrepService interviewPrepService) {
        this.interviewPrepService = interviewPrepService;
    }

    @GetMapping("/demo")
    public InterviewPrepDemo demo() {
        return interviewPrepService.getDemoPrep();
    }
}
