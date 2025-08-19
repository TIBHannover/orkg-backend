package org.orkg.common

import org.junit.jupiter.api.Test

internal class ThingIdSerializableTest {
    @Test
    fun `Given a ThingId instance, when serializing to byte array and deserializing back to ThingId, it successfully reconstructs the instance`() {
        testSerialization(ThingId("R123"))
    }
}
