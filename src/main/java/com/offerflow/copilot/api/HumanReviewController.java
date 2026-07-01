package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.HumanReviewCenter;
import com.offerflow.copilot.service.HumanReviewService;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
public class HumanReviewController {

    private final HumanReviewService humanReviewService;

    public HumanReviewController(HumanReviewService humanReviewService) {
        this.humanReviewService = humanReviewService;
    }

    @GetMapping
    public HumanReviewCenter reviews() {
        return humanReviewService.listReviews();
    }

    @GetMapping("/{id}")
    public HumanReviewCenter.ReviewDetail review(@PathVariable String id) {
        return humanReviewService.getReview(id);
    }

    @GetMapping("/{id}/audit-events")
    public List<HumanReviewCenter.AuditEvent> auditEvents(@PathVariable String id) {
        return humanReviewService.auditEvents(id);
    }

    @PostMapping("/{id}/confirm")
    public HumanReviewCenter.ReviewDetail confirm(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request) {
        return humanReviewService.confirm(id, actor(request), actorRole(request), humanNote(request));
    }

    @PostMapping("/{id}/return")
    public HumanReviewCenter.ReviewDetail returnForRevision(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request) {
        return humanReviewService.returnForRevision(id, actor(request), actorRole(request), humanNote(request));
    }

    @PostMapping("/{id}/flag-risk")
    public HumanReviewCenter.ReviewDetail flagRisk(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request) {
        return humanReviewService.flagRisk(id, actor(request), actorRole(request), humanNote(request));
    }

    private String actor(ReviewActionRequest request) {
        return request == null ? "" : request.actor();
    }

    private String actorRole(ReviewActionRequest request) {
        return request == null ? "" : request.actorRole();
    }

    private String humanNote(ReviewActionRequest request) {
        if (request == null) {
            return "";
        }
        return request.humanNote() == null || request.humanNote().isBlank() ? request.note() : request.humanNote();
    }

    public record ReviewActionRequest(String actor, String actorRole, String humanNote, String note) {
    }
}
