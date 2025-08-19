package org.orkg.common

import org.junit.jupiter.api.Test

internal class ContributorIdSerializableTest {
    @Test
    fun `Given a ContributorId instance, when serializing to byte array and deserializing back to ContributorId, it successfully reconstructs the instance`() {
        testSerialization(ContributorId("802977ac-aafe-4eef-ba16-57de189b9e9f"))
    }
}
