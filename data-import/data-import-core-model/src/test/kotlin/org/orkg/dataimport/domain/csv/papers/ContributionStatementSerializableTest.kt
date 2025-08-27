package org.orkg.dataimport.domain.csv.papers

import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.testSerialization
import org.orkg.dataimport.domain.TypedValue
import org.orkg.graph.domain.Classes

internal class ContributionStatementSerializableTest {
    @Test
    fun `Given a TypedCSVRecord instance, when serializing to byte array and deserializing back to TypedCSVRecord, it successfully reconstructs the instance`() {
        testSerialization(ContributionStatement(Either.right("pred"), TypedValue("resource", "DOI", Classes.resource)))
    }
}
