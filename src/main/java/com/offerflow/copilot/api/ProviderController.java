package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.ProviderStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/provider")
public class ProviderController {

    @GetMapping("/status")
    public ProviderStatus status() {
        return ProviderStatus.localRule();
    }
}
