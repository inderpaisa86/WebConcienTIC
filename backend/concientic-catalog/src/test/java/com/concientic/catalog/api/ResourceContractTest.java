package com.concientic.catalog.api;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceContractTest {

    @Test
    void pageResponseHasStablePaginationShape() {
        var page = PageResponse.of(java.util.List.of("resource-1"), 0, 24, 25);

        assertThat(page.items()).containsExactly("resource-1");
        assertThat(page.page()).isZero();
        assertThat(page.size()).isEqualTo(24);
        assertThat(page.total()).isEqualTo(25);
        assertThat(page.totalPages()).isEqualTo(2);
    }
}
