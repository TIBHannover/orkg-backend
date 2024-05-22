package org.orkg.contenttypes.domain.actions

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.MAX_LABEL_LENGTH

class DescriptionValidatorUnitTest {
    private val descriptionValidator = DescriptionValidator<String?, Unit> { it }

    @Test
    fun `Given a description, when valid, it returns success`() {
        assertDoesNotThrow { descriptionValidator("valid", Unit) }
    }

    @Test
    fun `Given a description, when null, it returns success`() {
        assertDoesNotThrow { descriptionValidator(null, Unit) }
    }

    @Test
    fun `Given a description, when invalid, it throws an exception`() {
        assertThrows<InvalidDescription> { descriptionValidator("a".repeat(MAX_LABEL_LENGTH + 1), Unit) }
    }
}
