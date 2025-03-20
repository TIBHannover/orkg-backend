package org.orkg.world

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SystemEnvironmentTest {
    @Test
    fun `System environment provides a value if present`() {
        val environment = SystemEnvironment()

        assertThat(environment["HOME"]).isNotNull()
    }

    @Test
    fun `System environment returns null if variable is not present`() {
        val environment = SystemEnvironment()

        assertThat(environment["__DOES_NOT_EXIST__"]).isNull()
    }

    @Test
    fun `correctly delegates to other environments in the chain`() {
        val environment = SystemEnvironment(MapBackedEnvironment(mapOf("NOT_IN_OUTER" to "fallback value")))

        assertThat(environment["HOME"]).isNotNull()
        assertThat(environment["NOT_IN_OUTER"]).isEqualTo("fallback value")
        assertThat(environment["NOT_PRESENT_ANYWHERE"]).isNull()
    }
}
