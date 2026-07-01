package com.offerflow.copilot.api;

import java.util.List;

import com.offerflow.copilot.domain.EvidenceCoverage;
import com.offerflow.copilot.domain.EvidenceLibrary;
import com.offerflow.copilot.service.EvidenceLibraryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{id}")
    public EvidenceLibrary.EvidenceItemDetail detail(@PathVariable String id) {
        return evidenceLibraryService.getDetail(id);
    }

    @GetMapping("/{id}/audit-events")
    public List<EvidenceLibrary.EvidenceAuditEvent> auditEvents(@PathVariable String id) {
        return evidenceLibraryService.auditEvents(id);
    }

    @PostMapping
    public EvidenceLibrary.EvidenceItemDetail createDraft(
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request) {
        return evidenceLibraryService.createDraft(orEmpty(request));
    }

    @PutMapping("/{id}")
    public EvidenceLibrary.EvidenceItemDetail updateDraft(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request) {
        return evidenceLibraryService.updateDraft(id, orEmpty(request));
    }

    @PostMapping("/{id}/confirm")
    public EvidenceLibrary.EvidenceItemDetail confirm(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request) {
        return evidenceLibraryService.confirm(id, orEmpty(request));
    }

    @PostMapping("/{id}/return-to-draft")
    public EvidenceLibrary.EvidenceItemDetail returnToDraft(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request) {
        return evidenceLibraryService.returnToDraft(id, orEmpty(request));
    }

    @PostMapping("/{id}/archive")
    public EvidenceLibrary.EvidenceItemDetail archive(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request) {
        return evidenceLibraryService.archive(id, orEmpty(request));
    }

    @PostMapping("/{id}/restore")
    public EvidenceLibrary.EvidenceItemDetail restore(
            @PathVariable String id,
            @RequestBody(required = false) EvidenceLibrary.EvidenceMutationRequest request) {
        return evidenceLibraryService.restore(id, orEmpty(request));
    }

    @GetMapping("/coverage")
    public EvidenceCoverage coverage() {
        return evidenceLibraryService.getCoverage();
    }

    private EvidenceLibrary.EvidenceMutationRequest orEmpty(EvidenceLibrary.EvidenceMutationRequest request) {
        if (request != null) {
            return request;
        }
        return new EvidenceLibrary.EvidenceMutationRequest(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
