package org.orkg.dataimport.domain.csv

import org.junit.jupiter.api.Test
import org.orkg.common.testSerialization

internal class CSVIDSerializableTest {
    @Test
    fun `Given a CSVID instance, when serializing to byte array and deserializing back to CSVID, it successfully reconstructs the instance`() {
        testSerialization(CSVID("6d57f7fd-5f34-4f1a-985d-affc3e22194b"))
    }
}
