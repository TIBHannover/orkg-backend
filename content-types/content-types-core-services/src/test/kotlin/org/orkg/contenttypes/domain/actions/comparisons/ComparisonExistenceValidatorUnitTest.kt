package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyComparison
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateComparisonCommand

internal class ComparisonExistenceValidatorUnitTest {
    private val comparisonService: ComparisonUseCases = mockk()

    private val comparisonExistenceValidator = ComparisonExistenceValidator(comparisonService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(comparisonService)
    }

    @Test
    fun `Given a comparison update command, when checking for comparison existence, it returns success`() {
        val comparison = createDummyComparison()
        val command = dummyUpdateComparisonCommand().copy(comparisonId = comparison.id)
        val state = UpdateComparisonState()

        every { comparisonService.findById(comparison.id) } returns Optional.of(comparison)

        comparisonExistenceValidator(command, state).asClue {
            it.comparison shouldBe comparison
        }

        verify(exactly = 1) { comparisonService.findById(comparison.id) }
    }

    @Test
    fun `Given a comparison update command, when checking for comparison existence and comparison is not found, it throws an exception`() {
        val comparison = createDummyComparison()
        val command = dummyUpdateComparisonCommand().copy(comparisonId = comparison.id)
        val state = UpdateComparisonState()

        every { comparisonService.findById(comparison.id) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { comparisonExistenceValidator(command, state) }

        verify(exactly = 1) { comparisonService.findById(comparison.id) }
    }
}
