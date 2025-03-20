package org.orkg.world

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SystemRandomnessTest {
    @Test
    fun `two different SystemRandomness instances provide the same RNG`() {
        val random1 = SystemRandomness()
        val random2 = SystemRandomness()
        assertThat(random1).isNotSameAs(random2)

        assertThat(random1.rng).isSameAs(random2.rng)
    }
}
