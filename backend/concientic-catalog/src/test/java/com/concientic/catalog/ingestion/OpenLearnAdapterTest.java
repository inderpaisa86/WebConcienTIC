package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenLearnAdapterTest {

    @Test
    void createsCandidatesFromConfiguredSeedsWithoutInventingMetadata() {
        var adapter = new OpenLearnAdapter(List.of(
                URI.create("https://www.open.edu/openlearn/example-one"),
                URI.create("https://www.open.edu/openlearn/example-two")));

        var candidates = adapter.discover();

        assertThat(candidates).hasSize(2);
        assertThat(candidates).allSatisfy(candidate -> {
            assertThat(candidate.sourceName()).isEqualTo("OpenLearn");
            assertThat(candidate.provider()).isEqualTo("OpenLearn");
            assertThat(candidate.expectedTitle()).isNull();
            assertThat(candidate.expectedLanguages()).isEmpty();
        });
    }
}
