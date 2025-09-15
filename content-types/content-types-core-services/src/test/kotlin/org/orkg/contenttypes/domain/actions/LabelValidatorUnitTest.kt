package org.orkg.contenttypes.domain.actions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH

internal class LabelValidatorUnitTest {
    private val labelValidator = LabelValidator<String?, Unit> { it }

    @Test
    fun `Given a label, when valid, it returns success`() {
        assertDoesNotThrow { labelValidator("valid", Unit) }
    }

    @Test
    fun `Given a label, when null, it returns success`() {
        assertDoesNotThrow { labelValidator(null, Unit) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["\n", "\u0000"])
    fun `Given a label, when invalid, it throws an exception`(label: String) {
        assertThrows<InvalidLabel> { labelValidator(label, Unit) }
    }

    @Test
    fun `Given a label, when too long, it throws an exception`() {
        assertThrows<InvalidLabel> { labelValidator("a".repeat(MAX_LABEL_LENGTH + 1), Unit) }
    }
}
