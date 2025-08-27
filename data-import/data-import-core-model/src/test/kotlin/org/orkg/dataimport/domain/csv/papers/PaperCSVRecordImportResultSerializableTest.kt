package org.orkg.dataimport.domain.csv.papers

import org.junit.jupiter.api.Test
import org.orkg.common.testSerialization
import org.orkg.dataimport.domain.testing.fixtures.createPaperCSVRecordImportResult

internal class PaperCSVRecordImportResultSerializableTest {
    @Test
    fun `Given a PaperCSVRecordImportResult instance, when serializing to byte array and deserializing back to PaperCSVRecordImportResult, it successfully reconstructs the instance`() {
        testSerialization(createPaperCSVRecordImportResult())
    }
}
