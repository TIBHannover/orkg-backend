package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
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
import org.junit.jupiter.api.assertThrows
import org.orkg.contenttypes.domain.ComparisonAlreadyPublished
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.PublishComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createPublishedComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishComparisonCommand
import org.orkg.contenttypes.output.ComparisonPublishedRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.testing.fixtures.createResource

class ComparisonPublishableValidatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val comparisonPublishedRepository: ComparisonPublishedRepository = mockk()

    private val comparisonPublishableValidator = ComparisonPublishableValidator(resourceService, comparisonPublishedRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, comparisonPublishedRepository)
    }

    @Test
    fun `Given a comparison publish command, when comparison is unpublished, it returns success`() {
        val comparison = createResource(classes = setOf(Classes.comparison))
        val command = dummyPublishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState()

        every { resourceService.findById(comparison.id) } returns Optional.of(comparison)
        every { comparisonPublishedRepository.findById(command.id) } returns Optional.empty()

        comparisonPublishableValidator(command, state).asClue {
            it.comparison shouldBe comparison
        }

        verify(exactly = 1) { resourceService.findById(comparison.id) }
        verify(exactly = 1) { comparisonPublishedRepository.findById(command.id) }
    }

    @Test
    fun `Given a comparison publish command, when comparison does not exist, it throws an exception`() {
        val command = dummyPublishComparisonCommand()
        val state = PublishComparisonState()

        every { resourceService.findById(command.id) } returns Optional.empty()

        assertThrows<ComparisonNotFound> { comparisonPublishableValidator(command, state) }

        verify(exactly = 1) { resourceService.findById(command.id) }
    }

    @Test
    fun `Given a comparison publish command, when comparison is already published, it throws an exception`() {
        val comparison = createResource(classes = setOf(Classes.comparison))
        val command = dummyPublishComparisonCommand().copy(id = comparison.id)
        val state = PublishComparisonState()

        every { resourceService.findById(command.id) } returns Optional.of(comparison)
        every { comparisonPublishedRepository.findById(command.id) } returns Optional.of(createPublishedComparison())

        assertThrows<ComparisonAlreadyPublished> { comparisonPublishableValidator(command, state) }

        verify(exactly = 1) { resourceService.findById(command.id) }
        verify(exactly = 1) { comparisonPublishedRepository.findById(command.id) }
    }
}
