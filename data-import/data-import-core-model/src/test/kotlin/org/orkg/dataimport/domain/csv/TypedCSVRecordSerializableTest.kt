package org.orkg.dataimport.domain.csv

import org.junit.jupiter.api.Test
import org.orkg.common.testSerialization
import org.orkg.dataimport.domain.testing.fixtures.createTypedCSVRecord

internal class TypedCSVRecordSerializableTest {
    @Test
    fun `Given a TypedCSVRecord instance, when serializing to byte array and deserializing back to TypedCSVRecord, it successfully reconstructs the instance`() {
        testSerialization(createTypedCSVRecord())
    }
}
