package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.ApplicationTracker;
import com.offerflow.copilot.service.ApplicationTrackerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applications")
public class ApplicationTrackerController {

    private final ApplicationTrackerService applicationTrackerService;

    public ApplicationTrackerController(ApplicationTrackerService applicationTrackerService) {
        this.applicationTrackerService = applicationTrackerService;
    }

    @GetMapping
    public ApplicationTracker applications() {
        return applicationTrackerService.getTracker();
    }
}
