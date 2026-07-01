package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.ProviderStatus;
import com.offerflow.copilot.domain.ProviderTraceCenter;
import com.offerflow.copilot.service.ProviderTraceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    private final ProviderTraceService providerTraceService;

    public ProviderController(ProviderTraceService providerTraceService) {
        this.providerTraceService = providerTraceService;
    }

    @GetMapping("/status")
    public ProviderStatus status() {
        return ProviderStatus.localRule();
    }

    @GetMapping("/settings")
    public ProviderTraceCenter.ProviderSettings settings() {
        return providerTraceService.settings();
    }

    @GetMapping("/traces")
    public ProviderTraceCenter.TraceIndex traces() {
        return providerTraceService.traces();
    }

    @GetMapping("/traces/{runId}")
    public ProviderTraceCenter.TraceRun trace(@PathVariable String runId) {
        return providerTraceService.trace(runId);
    }
}
