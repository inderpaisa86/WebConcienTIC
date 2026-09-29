package com.concientic.catalog.catalog;

import com.concientic.catalog.domain.ResourceResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogServiceTest {

    @Test
    void capsPageSizeAndKeepsPageMetadataConsistent() {
        var repository = mock(ResourceRepository.class);
        when(repository.count(null, null, null, null, null)).thenReturn(101L);
        when(repository.search(null, null, null, null, null, 0, 100)).thenReturn(List.<ResourceResponse>of());

        var result = new CatalogService(repository).search(null, null, null, null, null, -1, 1000);

        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(100);
        assertThat(result.total()).isEqualTo(101);
        assertThat(result.totalPages()).isEqualTo(2);
    }
}
