package com.concientic.catalog.api;

import com.concientic.catalog.review.ReviewQueueService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/internal/reviews")
public class InternalReviewController {
    private final ReviewQueueService reviewService;
    private final String configuredToken;

    public InternalReviewController(ReviewQueueService reviewService,
                                     @Value("${concientic.review.token:}") String configuredToken) {
        this.reviewService = reviewService;
        this.configuredToken = configuredToken;
    }

    @GetMapping
    public PageResponse<ReviewQueueItemResponse> search(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(defaultValue = "OPEN") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "24") int size) {
        authorize(authorization);
        return reviewService.search(status, page, size);
    }

    @GetMapping("/{reviewId}")
    public ReviewQueueDetailResponse get(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID reviewId) {
        authorize(authorization);
        return reviewService.get(reviewId);
    }

    @PostMapping("/{reviewId}/approve")
    public ReviewQueueDetailResponse approve(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID reviewId,
            @RequestBody ReviewDecisionRequest request) {
        authorize(authorization);
        return reviewService.approve(reviewId, request);
    }

    @PostMapping("/{reviewId}/correct")
    public ReviewQueueDetailResponse correct(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID reviewId,
            @RequestBody ReviewDecisionRequest request) {
        authorize(authorization);
        return reviewService.correct(reviewId, request);
    }

    @PostMapping("/{reviewId}/reject")
    public ReviewQueueDetailResponse reject(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @PathVariable UUID reviewId,
            @RequestBody ReviewDecisionRequest request) {
        authorize(authorization);
        return reviewService.reject(reviewId, request);
    }

    private void authorize(String authorization) {
        if (configuredToken.isBlank() || authorization == null || !authorization.equals("Bearer " + configuredToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid review token");
        }
    }
}
