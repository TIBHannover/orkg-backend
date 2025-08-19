package org.orkg.common

import io.kotest.matchers.shouldBe
import java.io.Serializable

inline fun <reified T : Serializable> testSerialization(instance: T) {
    val serialized = instance.serializeToByteArray()
    val reconstructed = serialized.deserializeToObject<T>()

    reconstructed shouldBe instance
}
