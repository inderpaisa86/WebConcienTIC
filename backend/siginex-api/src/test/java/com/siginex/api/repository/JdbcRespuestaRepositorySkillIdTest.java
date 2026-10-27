package com.siginex.api.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for the {@code skillId} derivation of {@link JdbcRespuestaRepository},
 * faithful to the original {@code guardarRespuestas}: the part before the first
 * {@code '-'}, or the whole id when there is none.
 */
class JdbcRespuestaRepositorySkillIdTest {

    @Test
    void derivaSkillIdDesdeLaParteAntesDelPrimerGuion() {
        assertThat(JdbcRespuestaRepository.skillId("sst-1.1.1")).isEqualTo("sst");
        assertThat(JdbcRespuestaRepository.skillId("ambiental-2.3")).isEqualTo("ambiental");
        assertThat(JdbcRespuestaRepository.skillId("gobernanza_ia-1.1")).isEqualTo("gobernanza_ia");
    }

    @Test
    void sinGuionDevuelveElIdCompleto() {
        assertThat(JdbcRespuestaRepository.skillId("sst")).isEqualTo("sst");
    }

    @Test
    void guionInicialDevuelveElIdCompleto() {
        // indexOf('-') == 0 is not > 0, so the whole id is used (faithful to the
        // original i > 0 guard).
        assertThat(JdbcRespuestaRepository.skillId("-raro")).isEqualTo("-raro");
    }
}
