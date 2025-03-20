package org.orkg.world

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WorldTest {
    @Test
    fun `provides the intended classes when created for testing`() {
        val world = World.forTesting()

        assertThat(world.environment).isInstanceOf(MapBackedEnvironment::class.java)
        assertThat(world.randomness).isInstanceOf(ControlledRandomness::class.java)
    }

    @Test
    fun `provides the intended classes when created for system access`() {
        val world = World.ofSystemState()

        assertThat(world.environment).isInstanceOf(SystemEnvironment::class.java)
        assertThat(world.randomness).isInstanceOf(SystemRandomness::class.java)
    }
}
