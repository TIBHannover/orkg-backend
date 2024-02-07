package org.orkg.contenttypes.domain.actions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.graph.domain.InvalidLabel

class LabelValidatorUnitTest {
    private val labelValidator = LabelValidator<String, Unit> { it }

    @Test
    fun `Given a label, when valid, it returns success`() {
        labelValidator("valid", Unit)
    }

    @Test
    fun `Given a label, when invalid, it throws an exception`() {
        assertThrows<InvalidLabel> { labelValidator("\n", Unit) }
    }
}
