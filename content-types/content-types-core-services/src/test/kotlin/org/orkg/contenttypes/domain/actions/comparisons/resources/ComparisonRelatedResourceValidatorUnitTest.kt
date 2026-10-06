package org.orkg.contenttypes.domain.actions.comparisons.resources

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
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceCommand
import org.orkg.contenttypes.domain.actions.CreateComparisonRelatedResourceState
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.InvalidDescription
import org.orkg.graph.domain.InvalidLabel
import org.orkg.graph.domain.MAX_LABEL_LENGTH
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional
import java.util.UUID

internal class ComparisonRelatedResourceValidatorUnitTest : MockkBaseTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val comparisonRelatedResourceValidator = ComparisonRelatedResourceValidator(resourceRepository)

    @Test
    fun `Given a comparison related resource create command, when inputs are valid, it returns success`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = "https://example.org/test.png",
            url = "https://orkg.org/resources/R1000",
            description = "comparison related resource description",
        )
        val comparison = createResource(classes = setOf(Classes.comparison))
        val state = CreateComparisonRelatedResourceState()

        every { resourceRepository.findById(command.comparisonId) } returns Optional.of(comparison)

        comparisonRelatedResourceValidator(command, state) shouldBe state

        verify(exactly = 1) { resourceRepository.findById(command.comparisonId) }
    }

    @Test
    fun `Given a comparison related resource create command, when label is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "\n",
            image = null,
            url = null,
            description = null,
        )
        val state = CreateComparisonRelatedResourceState()

        shouldThrow<InvalidLabel> { comparisonRelatedResourceValidator(command, state) }.asClue {
            it.property shouldBe "label"
        }
    }

    @Test
    fun `Given a comparison related resource create command, when image is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = "\n",
            url = null,
            description = null,
        )
        val state = CreateComparisonRelatedResourceState()

        shouldThrow<InvalidLabel> { comparisonRelatedResourceValidator(command, state) }.asClue {
            it.property shouldBe "image"
        }
    }

    @Test
    fun `Given a comparison related resource create command, when url is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = null,
            url = "\n",
            description = null,
        )
        val state = CreateComparisonRelatedResourceState()

        shouldThrow<InvalidLabel> { comparisonRelatedResourceValidator(command, state) }.asClue {
            it.property shouldBe "url"
        }
    }

    @Test
    fun `Given a comparison related resource create command, when description is invalid, it throws an exception`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = null,
            url = null,
            description = "a".repeat(MAX_LABEL_LENGTH + 1),
        )
        val state = CreateComparisonRelatedResourceState()

        shouldThrow<InvalidDescription> { comparisonRelatedResourceValidator(command, state) }.asClue {
            it.property shouldBe "description"
        }
    }

    @Test
    fun `Given a comparison related resource create command, when comparison does not exist, it throws an exception`() {
        val command = CreateComparisonRelatedResourceCommand(
            comparisonId = ThingId("R123"),
            contributorId = ContributorId(UUID.randomUUID()),
            label = "related resource",
            image = null,
            url = null,
            description = null,
        )
        val state = CreateComparisonRelatedResourceState()

        every { resourceRepository.findById(any()) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { comparisonRelatedResourceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }
}
