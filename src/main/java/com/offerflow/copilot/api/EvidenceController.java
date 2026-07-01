package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.EvidenceCoverage;
import com.offerflow.copilot.domain.EvidenceLibrary;
import com.offerflow.copilot.service.EvidenceLibraryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/evidence")
public class EvidenceController {

    private final EvidenceLibraryService evidenceLibraryService;

    public EvidenceController(EvidenceLibraryService evidenceLibraryService) {
        this.evidenceLibraryService = evidenceLibraryService;
    }

    @GetMapping("/library")
    public EvidenceLibrary library() {
        return evidenceLibraryService.getLibrary();
    }

    @GetMapping("/coverage")
    public EvidenceCoverage coverage() {
        return evidenceLibraryService.getCoverage();
    }
}
