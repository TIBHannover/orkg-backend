package org.orkg.dataimport.domain.csv.papers

import org.junit.jupiter.api.Test
import org.orkg.common.testSerialization
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecord

internal class PaperCSVRecordSerializableTest {
    @Test
    fun `Given a PaperCSVRecord instance, when serializing to byte array and deserializing back to PaperCSVRecord, it successfully reconstructs the instance`() {
        testSerialization(createPaperCSVRecord())
    }
}
