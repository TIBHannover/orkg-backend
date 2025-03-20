package org.orkg.world

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MapBackedEnvironmentTest {
    @Test
    fun `returns null for all values when no values were provided`() {
        val environment = MapBackedEnvironment()

        assertThat(environment["THIS_DOES_NOT_EXIST"]).isNull()
        assertThat(environment["NEITHER_DOES_THIS"]).isNull()
        assertThat(environment["AND_THIS_AS_WELL"]).isNull()
    }

    @Test
    fun `returns the values for all initialized variables`() {
        val environment = MapBackedEnvironment(
            mapOf(
                "BUT_THIS_DOES" to "first value",
                "AND_THIS_AS_WELL" to "second value",
            )
        )

        assertThat(environment["THIS_DOES_NOT_EXIST"]).isNull()
        assertThat(environment["BUT_THIS_DOES"]).isEqualTo("first value")
        assertThat(environment["AND_THIS_AS_WELL"]).isEqualTo("second value")
    }

    @Test
    fun `correctly delegates to other environments in the chain`() {
        val environment =
            MapBackedEnvironment(
                initialMap = mapOf("EXISTS_IN_OUTER" to "true"),
                next = MapBackedEnvironment(
                    initialMap = mapOf("EXISTS_IN_MIDDLE" to "true"),
                    next = MapBackedEnvironment(
                        initialMap = mapOf("EXISTS_IN_INNER" to "true"),
                        next = null
                    )
                )
            )

        assertThat(environment["EXISTS_IN_OUTER"]).isEqualTo("true")
        assertThat(environment["EXISTS_IN_MIDDLE"]).isEqualTo("true")
        assertThat(environment["EXISTS_IN_INNER"]).isEqualTo("true")
        assertThat(environment["DOES_NOT_EXIST_ANYWHERE"]).isNull()
    }
}
