package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IngestionRunServiceTest {

    @Test
    void persistsSuccessfulCycleAndReturnsItsRunId() {
        var research = mock(ResearchCycleService.class);
        var runStore = mock(IngestionRunStore.class);
        var observationStore = mock(ResourceObservationStore.class);
        var cycle = new ResearchCycleResult(2, 2, 1, 1, List.of(), List.of());
        when(research.execute()).thenReturn(cycle);
        when(observationStore.persist(any(), eq(cycle)))
                .thenReturn(new ResourceObservationStore.PersistedCounts(2, 2, 1));

        var result = new IngestionRunService(research, runStore, observationStore, "America/Bogota").execute();

        assertThat(result.runId()).isNotNull();
        assertThat(result.cycle()).isSameAs(cycle);
        assertThat(result.persistedCounts().resourcesPersisted()).isEqualTo(2);
        verify(runStore).start(eq(result.runId()), any(), eq("America/Bogota"));
        verify(runStore).finish(eq(result.runId()), eq("SUCCESS"), eq(cycle), eq(null));
    }

    @Test
    void marksRunFailedWhenResearchThrows() {
        var research = mock(ResearchCycleService.class);
        var runStore = mock(IngestionRunStore.class);
        var observationStore = mock(ResourceObservationStore.class);
        when(research.execute()).thenThrow(new IllegalStateException("source unavailable"));

        var service = new IngestionRunService(research, runStore, observationStore, "America/Bogota");

        org.assertj.core.api.Assertions.assertThatThrownBy(service::execute)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("source unavailable");
        verify(runStore).finish(any(), eq("FAILED"), any(ResearchCycleResult.class), eq("source unavailable"));
    }
}
