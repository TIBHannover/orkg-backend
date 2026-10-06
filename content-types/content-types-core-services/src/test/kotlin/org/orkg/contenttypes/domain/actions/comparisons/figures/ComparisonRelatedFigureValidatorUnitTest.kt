package org.orkg.contenttypes.domain.actions.comparisons.figures

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedFigureState
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional
import java.util.UUID

internal class ComparisonRelatedFigureValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val comparisonRelatedFigureValidator = ComparisonRelatedFigureValidator(resourceRepository)

    @Test
    fun `Given a comparison related figure create command, when inputs are valid, it returns success`() {
        val command = CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related figure",
            image = "https://example.org/test.png",
            description = "comparison related figure description",
        )
        val comparison = createResource(classes = setOf(Classes.comparison))
        val state = CreateComparisonRelatedFigureState()

        every { resourceRepository.findById(any()) } returns Optional.of(comparison)

        comparisonRelatedFigureValidator(command, state) shouldBe state

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }

    @Test
    fun `Given a comparison related figure create command, when label is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "\n",
            image = null,
            description = null,
        )
        val state = CreateComparisonRelatedFigureState()

        shouldThrow<InvalidLabel> { comparisonRelatedFigureValidator(command, state) }.asClue {
            it.property shouldBe "label"
        }
    }

    @Test
    fun `Given a comparison related figure create command, when image is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related figure",
            image = "\n",
            description = null,
        )
        val state = CreateComparisonRelatedFigureState()

        shouldThrow<InvalidLabel> { comparisonRelatedFigureValidator(command, state) }.asClue {
            it.property shouldBe "image"
        }
    }

    @Test
    fun `Given a comparison related figure create command, when description is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related figure",
            image = null,
            description = "a".repeat(MAX_LABEL_LENGTH + 1),
        )
        val state = CreateComparisonRelatedFigureState()

        shouldThrow<InvalidDescription> { comparisonRelatedFigureValidator(command, state) }.asClue {
            it.property shouldBe "description"
        }
    }

    @Test
    fun `Given a comparison related figure create command, when comparison does not exist, it throws an exception`() {
        val command = CreateComparisonRelatedFigureCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related figure",
            image = null,
            description = null,
        )
        val state = CreateComparisonRelatedFigureState()

        every { resourceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { comparisonRelatedFigureValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }
}
