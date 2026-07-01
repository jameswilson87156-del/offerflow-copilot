package com.offerflow.copilot.service;

import com.offerflow.copilot.domain.InterviewPrepDemo;
import com.offerflow.copilot.persistence.JsonCodec;
import com.offerflow.copilot.persistence.PersistenceSeedService;
import com.offerflow.copilot.persistence.entity.InterviewPrepEntity;
import com.offerflow.copilot.persistence.repository.InterviewPrepRepository;
import com.offerflow.copilot.persistence.repository.JobPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class InterviewPrepService {

    private final InterviewPrepRepository interviewPrepRepository;
    private final JobPostRepository jobPostRepository;
    private final JsonCodec jsonCodec;

    public InterviewPrepService(
            InterviewPrepRepository interviewPrepRepository,
            JobPostRepository jobPostRepository,
            JsonCodec jsonCodec) {
        this.interviewPrepRepository = interviewPrepRepository;
        this.jobPostRepository = jobPostRepository;
        this.jsonCodec = jsonCodec;
    }

    public InterviewPrepDemo getDemoPrep() {
        InterviewPrepEntity prep = interviewPrepRepository.findDemo()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Interview prep demo data not found"));
        String jobTitle = jobPostRepository.findById(prep.getJobId())
                .map((job) -> job.getTitle())
                .orElse(PersistenceSeedService.DEMO_JOB_ID);
        return new InterviewPrepDemo(
                prep.getMode(),
                prep.getId(),
                jobTitle,
                prep.getPositioningNotice(),
                jsonCodec.readList(prep.getFocusAreasJson(), InterviewPrepDemo.FocusArea.class),
                jsonCodec.readList(prep.getQuestionsJson(), InterviewPrepDemo.QuestionGroup.class),
                jsonCodec.read(prep.getStarDraftJson(), InterviewPrepDemo.StarDraft.class),
                jsonCodec.readList(prep.getRiskNotesJson(), String.class),
                jsonCodec.readList(prep.getReviewTimelineJson(), InterviewPrepDemo.TimelineStep.class),
                prep.getReviewStatus(),
                prep.getDisclaimer());
    }
}
