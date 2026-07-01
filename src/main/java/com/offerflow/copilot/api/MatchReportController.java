package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.MatchReportDemo;
import com.offerflow.copilot.service.MatchReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/match-report")
public class MatchReportController {

    private final MatchReportService matchReportService;

    public MatchReportController(MatchReportService matchReportService) {
        this.matchReportService = matchReportService;
    }

    @GetMapping("/demo")
    public MatchReportDemo demo() {
        return matchReportService.getDemoReport();
    }
}
