package org.orkg.common

import org.junit.jupiter.api.Test

internal class EitherSerializableTest {
    @Test
    fun `Given an Either-Left instance, when serializing to byte array and deserializing back to Either-Left, it successfully reconstructs the instance`() {
        testSerialization(Either.left<String, String>("left"))
    }

    @Test
    fun `Given an Either-Right instance, when serializing to byte array and deserializing back to Either-Right, it successfully reconstructs the instance`() {
        testSerialization(Either.right<String, String>("right"))
    }
}
