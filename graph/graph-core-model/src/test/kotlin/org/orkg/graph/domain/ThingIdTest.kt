package org.orkg.graph.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId

@DisplayName("A thing id")
class ThingIdTest {
    @Test
    fun `can not be blank`() {
        assertThrows<IllegalArgumentException> { ThingId("") }
    }

    @Test
    fun `should forbid injection attacks`() {
        assertThrows<IllegalArgumentException> {
            ThingId("{id: '\\'}; MATCH ( n ) DETACH DELETE n")
        }
    }

    @Test
    fun `should accept alphanumeric, dashes and underscores`() {
        assertDoesNotThrow {
            ThingId("iua:sdne98798-qdas-a-__2eqw8a:sBUAD")
        }
    }
}
