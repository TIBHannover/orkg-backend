package org.orkg.world

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

class ControlledRandomnessTest {
    @Test
    fun `two instances with no default seed provide the same results`() {
        val rng1 = ControlledRandomness().rng
        val rng2 = ControlledRandomness().rng
        assertThat(rng1).isNotSameAs(rng2)

        assertThat(rng1.produceValues()).isEqualTo(rng2.produceValues())
    }

    @Test
    fun `two instances with the same custom seed provide the same results`() {
        val rng1 = ControlledRandomness(23).rng
        val rng2 = ControlledRandomness(23).rng
        assertThat(rng1).isNotSameAs(rng2)

        assertThat(rng1.produceValues()).isEqualTo(rng2.produceValues())
    }

    @Test
    fun `two instances with different custom seed provide different results`() {
        val rng1 = ControlledRandomness(23).rng
        val rng2 = ControlledRandomness(42).rng
        assertThat(rng1).isNotSameAs(rng2)

        assertThat(rng1.produceValues()).isNotEqualTo(rng2.produceValues())
    }
}
