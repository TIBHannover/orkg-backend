package org.orkg.contenttypes.domain.actions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.graph.domain.InvalidLabel

internal class LabelCollectionValidatorUnitTest {
    private val labelCollectionValidator = LabelCollectionValidator<Collection<String>?, Unit>("labels") { it }

    @Test
    fun `Given a label, when valid, it returns success`() {
        assertDoesNotThrow { labelCollectionValidator(listOf("valid", "also valid"), Unit) }
    }

    @Test
    fun `Given a label, when emtpy, it returns success`() {
        assertDoesNotThrow { labelCollectionValidator(emptyList(), Unit) }
    }

    @Test
    fun `Given a label, when null, it returns success`() {
        assertDoesNotThrow { labelCollectionValidator(null, Unit) }
    }

    @Test
    fun `Given a label, when invalid, it throws an exception`() {
        assertThrows<InvalidLabel> { labelCollectionValidator(listOf("valid", "\n"), Unit) }
    }
}
