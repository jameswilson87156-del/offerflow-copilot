package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.DashboardSummary;
import com.offerflow.copilot.domain.ProviderStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return new DashboardSummary(3, 2, 5, ProviderStatus.localRule());
    }
}
