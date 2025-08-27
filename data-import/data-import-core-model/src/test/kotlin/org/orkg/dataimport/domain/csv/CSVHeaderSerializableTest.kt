package org.orkg.dataimport.domain.csv

import org.junit.jupiter.api.Test
import org.orkg.common.testSerialization
import org.orkg.graph.domain.Classes

internal class CSVHeaderSerializableTest {
    @Test
    fun `Given a CSVHeader instance, when serializing to byte array and deserializing back to CSVHeader, it successfully reconstructs the instance`() {
        testSerialization(CSVHeader(2, "the only possible value", "closed-namespace", Classes.boolean))
    }
}
