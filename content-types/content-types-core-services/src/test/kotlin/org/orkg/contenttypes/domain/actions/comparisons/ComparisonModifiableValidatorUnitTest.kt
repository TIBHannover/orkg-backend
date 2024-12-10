package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateComparisonCommand

internal class ComparisonModifiableValidatorUnitTest {
    private val comparisonModifiableValidator = ComparisonModifiableValidator()

    @Test
    fun `Given a comparison update command, when comparison is not published, it returns success`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState(
            comparison = createComparison().copy(published = false)
        )

        val result = comparisonModifiableValidator(command, state)

        result.asClue {
            it.comparison shouldBe state.comparison
            it.authors shouldBe state.authors
        }
    }

    @Test
    fun `Given a comparison update command, when comparison is published, it throws an exception`() {
        val command = dummyUpdateComparisonCommand()
        val state = UpdateComparisonState(
            comparison = createComparison().copy(published = true)
        )

        assertThrows<ComparisonNotModifiable> { comparisonModifiableValidator(command, state) }
    }
}
