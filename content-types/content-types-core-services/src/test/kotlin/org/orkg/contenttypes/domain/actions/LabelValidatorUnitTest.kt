package org.orkg.contenttypes.domain.actions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.graph.domain.InvalidLabel

class LabelValidatorUnitTest {
    private val labelValidator = LabelValidator<String?, Unit> { it }

    @Test
    fun `Given a label, when valid, it returns success`() {
        assertDoesNotThrow { labelValidator("valid", Unit) }
    }

    @Test
    fun `Given a label, when null, it returns success`() {
        assertDoesNotThrow { labelValidator(null, Unit) }
    }

    @Test
    fun `Given a label, when invalid, it throws an exception`() {
        assertThrows<InvalidLabel> { labelValidator("\n", Unit) }
    }
}
