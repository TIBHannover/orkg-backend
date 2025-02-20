package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.testing.fixtures.updateComparisonCommand
import java.util.Optional

internal class ComparisonExistenceValidatorUnitTest : MockkBaseTest {
    private val comparisonService: ComparisonUseCases = mockk()

    private val comparisonExistenceValidator = ComparisonExistenceValidator(comparisonService)

    @Test
    fun `Given a comparison update command, when checking for comparison existence, it returns success`() {
        val comparison = createComparison()
        val command = updateComparisonCommand().copy(comparisonId = comparison.id)
        val state = UpdateComparisonState()

        every { comparisonService.findById(comparison.id) } returns Optional.of(comparison)

        comparisonExistenceValidator(command, state).asClue {
            it.comparison shouldBe comparison
        }

        verify(exactly = 1) { comparisonService.findById(comparison.id) }
    }

    @Test
    fun `Given a comparison update command, when checking for comparison existence and comparison is not found, it throws an exception`() {
        val comparison = createComparison()
        val command = updateComparisonCommand().copy(comparisonId = comparison.id)
        val state = UpdateComparisonState()

        every { comparisonService.findById(comparison.id) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { comparisonExistenceValidator(command, state) }

        verify(exactly = 1) { comparisonService.findById(comparison.id) }
    }
}
