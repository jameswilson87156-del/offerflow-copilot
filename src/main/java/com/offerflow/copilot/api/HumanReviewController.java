package com.offerflow.copilot.api;

import com.offerflow.copilot.domain.HumanReviewCenter;
import com.offerflow.copilot.service.HumanReviewService;
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

    @PostMapping("/{id}/confirm")
    public HumanReviewCenter.ReviewDetail confirm(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request) {
        return humanReviewService.confirm(id, note(request));
    }

    @PostMapping("/{id}/return")
    public HumanReviewCenter.ReviewDetail returnForRevision(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request) {
        return humanReviewService.returnForRevision(id, note(request));
    }

    @PostMapping("/{id}/flag-risk")
    public HumanReviewCenter.ReviewDetail flagRisk(
            @PathVariable String id,
            @RequestBody(required = false) ReviewActionRequest request) {
        return humanReviewService.flagRisk(id, note(request));
    }

    private String note(ReviewActionRequest request) {
        return request == null ? "" : request.note();
    }

    public record ReviewActionRequest(String note) {
    }
}
