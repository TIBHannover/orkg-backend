package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.NullSource
import org.junit.jupiter.params.provider.ValueSource
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.PaperNotModifiable

internal class PaperModifiableValidatorUnitTest {
    private val paperModifiableValidator = PaperModifiableValidator<ThingId, Boolean?>({ it }, { it })

    @ParameterizedTest
    @NullSource
    @ValueSource(booleans = [true])
    fun `Given a paper update command, when paper is modifiable, it returns success`(modifiable: Boolean?) {
        shouldNotThrow<PaperNotModifiable> { paperModifiableValidator(ThingId("R123"), modifiable) }
    }

    @Test
    fun `Given a paper update command, when paper is not modifiable, it throws an exception`() {
        shouldThrow<PaperNotModifiable> { paperModifiableValidator(ThingId("R123"), false) }
    }
}
