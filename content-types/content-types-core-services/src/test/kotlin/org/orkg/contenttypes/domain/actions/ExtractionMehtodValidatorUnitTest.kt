package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidExtractionMethodChange

internal class ExtractionMehtodValidatorUnitTest : MockkBaseTest {
    private val extractionMehtodValidator = ExtractionMethodValidator<ExtractionMethod?, ExtractionMethod>({ it }, { it })

    @Test
    fun `Given a new and an old extraction method, when validating and transition is valid, it returns success`() {
        shouldNotThrow<InvalidExtractionMethodChange> {
            extractionMehtodValidator(ExtractionMethod.UNKNOWN, ExtractionMethod.MANUAL)
        }
    }

    @Test
    fun `Given a new and an old extraction method, when validating and transition is invalid, it throws an exception`() {
        shouldThrow<InvalidExtractionMethodChange> {
            extractionMehtodValidator(ExtractionMethod.UNKNOWN, ExtractionMethod.AI_GENERATED)
        }
    }

    @Test
    fun `Given a new and an old extraction method, when validating and extraction methods are equal, it returns success`() {
        shouldNotThrow<InvalidExtractionMethodChange> {
            extractionMehtodValidator(ExtractionMethod.AI_GENERATED, ExtractionMethod.AI_GENERATED)
        }
    }

    @Test
    fun `Given a extraction method, when new extraction method is unset, it returns success`() {
        shouldNotThrow<InvalidExtractionMethodChange> {
            extractionMehtodValidator(null, ExtractionMethod.UNKNOWN)
        }
    }
}
