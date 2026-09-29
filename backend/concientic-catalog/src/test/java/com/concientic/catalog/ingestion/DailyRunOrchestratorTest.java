package com.concientic.catalog.ingestion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DailyRunOrchestratorTest {

    @Test
    void exposesTheConfiguredPipelineWithoutClaimingResearchWasExecuted() {
        var orchestrator = new DailyRunOrchestrator("America/Bogota");

        var result = orchestrator.startDryRun();

        assertThat(result.status()).isEqualTo("NOT_WIRED");
        assertThat(result.dryRun()).isTrue();
        assertThat(result.stages()).isEqualTo(PipelineStages.ORDER);
        assertThat(result.warnings()).isNotEmpty();
    }
}
