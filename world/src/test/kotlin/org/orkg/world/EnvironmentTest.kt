package org.orkg.world

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class EnvironmentTest {
    @Test
    fun `passing null to break the chain works`() {
        class NoNext : Environment(null) {
            override fun getValue(variable: String): String? = null
        }

        val env: Environment = NoNext()

        assertThat(env["any variable"]).isNull()
    }

    @Test
    fun `passing another instance to delegate to works`() {
        class AnyValueWorks : Environment(null) {
            override fun getValue(variable: String): String? = "fallback value"
        }

        class DelegateToChain : Environment(AnyValueWorks()) {
            override fun getValue(variable: String): String? = null
        }

        val env: Environment = DelegateToChain()

        assertThat(env["any variable"]).isEqualTo("fallback value")
    }
}
