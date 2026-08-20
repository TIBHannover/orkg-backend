package org.orkg.contenttypes.domain.actions

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.InvalidExtractionMethodChange

internal class ExtractionMethodValidatorUnitTest : MockkBaseTest {
    private val extractionMethodValidator =
        ExtractionMethodValidator<Pair<ExtractionMethod?, String?>, Pair<ExtractionMethod, String>>(
            newValueSelector = { it.first },
            oldValueSelector = { it.first },
            newLabelSelector = { it.second },
            oldLabelSelector = { it.second },
        )

    @ParameterizedTest
    @MethodSource("validCombinations")
    fun `Given a new extraction method and label combination, when transition is valid, it returns success`(
        command: Pair<ExtractionMethod?, String?>,
        state: Pair<ExtractionMethod, String>,
    ) {
        shouldNotThrow<InvalidExtractionMethodChange> { extractionMethodValidator(command, state) }
    }

    @ParameterizedTest
    @MethodSource("invalidCombinations")
    fun `Given a new extraction method and label combination, when transition is invalid, it throws an exception`(
        command: Pair<ExtractionMethod?, String?>,
        state: Pair<ExtractionMethod, String>,
    ) {
        shouldThrow<InvalidExtractionMethodChange> { extractionMethodValidator(command, state) }
    }

    private companion object {
        @JvmStatic
        private fun validCombinations() = listOf(
            Arguments.of(ExtractionMethod.UNKNOWN to null, ExtractionMethod.MANUAL to "irrelevant"),
            Arguments.of(ExtractionMethod.AI_GENERATED to null, ExtractionMethod.AI_GENERATED to "irrelevant"),
            Arguments.of(null to null, ExtractionMethod.UNKNOWN to "irrelevant"),
            Arguments.of(ExtractionMethod.MANUAL to "new", ExtractionMethod.AI_GENERATED to "old"),
            Arguments.of(ExtractionMethod.UNKNOWN to "new", ExtractionMethod.MANUAL to "old"),
            Arguments.of(ExtractionMethod.AI_GENERATED to "new", ExtractionMethod.AI_GENERATED to "old"),
            Arguments.of(null to "new", ExtractionMethod.UNKNOWN to "old"),
        )

        @JvmStatic
        private fun invalidCombinations() = listOf(
            Arguments.of(ExtractionMethod.UNKNOWN to null, ExtractionMethod.AI_GENERATED to "irrelevant"),
            Arguments.of(ExtractionMethod.MANUAL to null, ExtractionMethod.AI_GENERATED to "irrelevant"),
            Arguments.of(ExtractionMethod.UNKNOWN to null, ExtractionMethod.AI_GENERATED_WITH_MANUAL_REVIEW to "irrelevant"),
            Arguments.of(ExtractionMethod.MANUAL to null, ExtractionMethod.AI_GENERATED_WITH_MANUAL_REVIEW to "irrelevant"),
        )
    }
}
