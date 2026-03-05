package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonDataSource
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.RequiresAtLeastTwoSources
import org.orkg.contenttypes.domain.actions.PublishComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.ComparisonUseCases
import org.orkg.contenttypes.input.testing.fixtures.publishComparisonCommand
import java.util.Optional

internal class ComparisonPublishableValidatorUnitTest : MockkBaseTest {
    private val comparisonService: ComparisonUseCases = mockk()

    private val comparisonPublishableValidator = ComparisonPublishableValidator(comparisonService)

    @Test
    fun `Given a comparison publish command, when comparison is unpublished, it returns success`() {
        val comparison = createComparison()
        val command = publishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState()

        every { comparisonService.findById(comparison.id) } returns Optional.of(comparison)

        comparisonPublishableValidator(command, state).asClue {
            it.comparison shouldBe comparison
        }

        verify(exactly = 1) { comparisonService.findById(comparison.id) }
    }

    @Test
    fun `Given a comparison publish command, when comparison does not exist, it throws an exception`() {
        val command = publishComparisonCommand()
        val state = PublishComparisonState()

        every { comparisonService.findById(command.id) } returns Optional.empty()

        assertThrows<ComparisonNotFound> { comparisonPublishableValidator(command, state) }

        verify(exactly = 1) { comparisonService.findById(command.id) }
    }

    @Test
    fun `Given a comparison publish command, when comparison is already published, it throws an exception`() {
        val comparison = createComparison().copy(published = true)
        val command = publishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState()

        every { comparisonService.findById(comparison.id) } returns Optional.of(comparison)

        assertThrows<ComparisonAlreadyPublished> { comparisonPublishableValidator(command, state) }

        verify(exactly = 1) { comparisonService.findById(command.id) }
    }

    @Test
    fun `Given a comparison publish command, when comparison has less than two sources, it throws an exception`() {
        val comparison = createComparison().copy(
            sources = listOf(ComparisonDataSource(ThingId("R123456"), ComparisonDataSource.Type.THING)),
        )
        val command = publishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState()

        every { comparisonService.findById(command.id) } returns Optional.of(comparison)

        assertThrows<RequiresAtLeastTwoSources> { comparisonPublishableValidator(command, state) }

        verify(exactly = 1) { comparisonService.findById(command.id) }
    }
}
