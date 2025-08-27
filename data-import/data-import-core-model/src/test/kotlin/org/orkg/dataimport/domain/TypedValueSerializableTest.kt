package org.orkg.dataimport.domain

import org.junit.jupiter.api.Test
import org.orkg.common.testSerialization
import org.orkg.graph.domain.Classes

internal class TypedValueSerializableTest {
    @Test
    fun `Given a TypedValue instance, when serializing to byte array and deserializing back to TypedValue, it successfully reconstructs the instance`() {
        testSerialization(TypedValue("resource", "DOI", Classes.resource))
    }
}
