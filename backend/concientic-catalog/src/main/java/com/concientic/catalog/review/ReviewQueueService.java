package com.concientic.catalog.review;

import com.concientic.catalog.api.PageResponse;
import com.concientic.catalog.api.ResourceCheckResponse;
import com.concientic.catalog.api.ReviewDecisionRequest;
import com.concientic.catalog.api.ReviewQueueDetailResponse;
import com.concientic.catalog.api.ReviewQueueItemResponse;
import com.concientic.catalog.domain.FreeStatus;
import com.concientic.catalog.domain.ResourceStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ReviewQueueService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final List<FreeStatus> PUBLISHABLE_FREE_STATUSES = List.of(
            FreeStatus.FREE, FreeStatus.FREE_CONTENT_PAID_CERTIFICATE,
            FreeStatus.AUDIT_FREE, FreeStatus.PARTIAL_FREE);
    private static final List<ResourceStatus> PUBLISHABLE_RESOURCE_STATUSES = List.of(
            ResourceStatus.ACTIVE, ResourceStatus.UPDATED,
            ResourceStatus.VERIFIED, ResourceStatus.LINK_CHANGED);

    private final ReviewQueueRepository repository;

    public ReviewQueueService(ReviewQueueRepository repository) {
        this.repository = repository;
    }

    public PageResponse<ReviewQueueItemResponse> search(String status, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String normalizedStatus = status == null || status.isBlank() ? null : status.trim().toUpperCase();
        long total = repository.count(normalizedStatus);
        return PageResponse.of(repository.search(normalizedStatus, safePage * safeSize, safeSize), safePage, safeSize, total);
    }

    public ReviewQueueDetailResponse get(UUID reviewId) {
        ReviewQueueItemResponse review = repository.find(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review item not found"));
        List<ResourceCheckResponse> checks = repository.checksFor(review.resourceId());
        return new ReviewQueueDetailResponse(review, checks);
    }

    @Transactional
    public ReviewQueueDetailResponse approve(UUID reviewId, ReviewDecisionRequest request) {
        return decide(reviewId, request, "APPROVED", "RESOLVED", "APPROVE", false);
    }

    @Transactional
    public ReviewQueueDetailResponse correct(UUID reviewId, ReviewDecisionRequest request) {
        return decide(reviewId, request, "CORRECTED", "RESOLVED", "CORRECT", true);
    }

    @Transactional
    public ReviewQueueDetailResponse reject(UUID reviewId, ReviewDecisionRequest request) {
        ReviewQueueItemResponse review = openReview(reviewId);
        String reviewerId = required(request, "reviewerId");
        String notes = required(request, "notes");
        if (repository.resolve(reviewId, review.version(), "REJECTED", reviewerId, "REJECTED", notes) != 1) {
            throw conflict();
        }
        if (review.resourceId() != null) {
            repository.saveHumanDecision(review.resourceId(), "REJECTED", reviewerId, notes,
                    review.title(), null, review.freeStatus(), ResourceStatus.REJECTED.name());
            repository.updateResource(review.resourceId(),
                    review.title() == null || review.title().isBlank() ? "Recurso rechazado" : review.title(),
                    null, review.freeStatus() == null ? FreeStatus.UNKNOWN.name() : review.freeStatus(),
                    ResourceStatus.REJECTED.name(), notes);
        }
        repository.audit(reviewId, review.resourceId(), "REJECT", reviewerId, notes,
                jsonPayload(request, "REJECTED"));
        return get(reviewId);
    }

    private ReviewQueueDetailResponse decide(UUID reviewId, ReviewDecisionRequest request, String decision,
                                             String queueStatus, String action, boolean correction) {
        ReviewQueueItemResponse review = openReview(reviewId);
        String reviewerId = required(request, "reviewerId");
        String title = request == null || request.title() == null || request.title().isBlank()
                ? review.title() : request.title().trim();
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "A title is required");
        }
        String freeStatus = validFreeStatus(request == null ? null : request.freeStatus());
        String resourceStatus = validResourceStatus(request == null ? null : request.resourceStatus());
        String notes = request == null ? null : request.notes();
        if (review.resourceId() == null) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Review item has no resource");
        }
        if (repository.resolve(reviewId, review.version(), queueStatus, reviewerId, decision, notes) != 1) {
            throw conflict();
        }
        repository.saveHumanDecision(review.resourceId(), decision, reviewerId, notes, title,
                request == null ? null : request.description(), freeStatus, resourceStatus);
        repository.updateResource(review.resourceId(), title, request == null ? null : request.description(),
                freeStatus, resourceStatus, notes);
        repository.audit(reviewId, review.resourceId(), action, reviewerId, notes,
                jsonPayload(request, decision));
        return get(reviewId);
    }

    private ReviewQueueItemResponse openReview(UUID reviewId) {
        ReviewQueueItemResponse review = repository.find(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review item not found"));
        if (!"OPEN".equals(review.status())) {
            throw conflict();
        }
        return review;
    }

    private static String required(ReviewDecisionRequest request, String field) {
        String value = request == null ? null : "reviewerId".equals(field) ? request.reviewerId() : request.notes();
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, field + " is required");
        }
        return value.trim();
    }

    private static String validFreeStatus(String value) {
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "freeStatus is required");
        }
        try {
            FreeStatus status = FreeStatus.valueOf(value.trim().toUpperCase());
            if (!PUBLISHABLE_FREE_STATUSES.contains(status)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "freeStatus is not publishable");
            }
            return status.name();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid freeStatus");
        }
    }

    private static String validResourceStatus(String value) {
        String normalized = value == null || value.isBlank() ? ResourceStatus.VERIFIED.name() : value.trim().toUpperCase();
        try {
            ResourceStatus status = ResourceStatus.valueOf(normalized);
            if (!PUBLISHABLE_RESOURCE_STATUSES.contains(status)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "resourceStatus is not publishable");
            }
            return status.name();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Invalid resourceStatus");
        }
    }

    private static ResponseStatusException conflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Review item is already resolved or version is stale");
    }

    private static String jsonPayload(ReviewDecisionRequest request, String decision) {
        if (request == null) return "{\"decision\":\"" + decision + "\"}";
        return "{\"decision\":\"" + decision + "\",\"title\":\"" + escape(request.title())
                + "\",\"freeStatus\":\"" + escape(request.freeStatus()) + "\",\"resourceStatus\":\""
                + escape(request.resourceStatus()) + "\"}";
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
