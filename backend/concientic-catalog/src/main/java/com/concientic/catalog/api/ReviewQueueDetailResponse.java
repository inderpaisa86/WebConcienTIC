package com.concientic.catalog.api;

import java.util.List;

public record ReviewQueueDetailResponse(
        ReviewQueueItemResponse review,
        List<ResourceCheckResponse> checks) {
}
