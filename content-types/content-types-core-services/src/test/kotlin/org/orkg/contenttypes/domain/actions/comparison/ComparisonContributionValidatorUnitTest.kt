package org.orkg.contenttypes.domain.actions.comparison

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
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.RequiresAtLeastTwoContributions
import org.orkg.contenttypes.domain.actions.ComparisonState
import org.orkg.contenttypes.testing.fixtures.dummyCreateComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

class ComparisonContributionValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val contributionValidator = ComparisonContributionValidator(resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository)
    }

    @Test
    fun `Given a comparison create command, when validating its contributions, it returns success`() {
        val command = dummyCreateComparisonCommand()
        val state = ComparisonState()
        val contribution = createResource(
            classes = setOf(Classes.contribution)
        )

        every { resourceRepository.findById(any()) } returns Optional.of(contribution)

        val result = contributionValidator(command, state)

        result.asClue {
            it.authors.size shouldBe 0
            it.comparisonId shouldBe null
        }

        command.contributions.forEach {
            verify(exactly = 1) { resourceRepository.findById(it) }
        }
    }

    @Test
    fun `Given a comparison create command, when contribution is missing, it throws an exception`() {
        val command = dummyCreateComparisonCommand()
        val state = ComparisonState()

        every { resourceRepository.findById(command.contributions.first()) } returns Optional.empty()

        assertThrows<ContributionNotFound> { contributionValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(command.contributions.first()) }
    }

    @Test
    fun `Given a comparison create command, when resource its not a contribution, it throws an exception`() {
        val command = dummyCreateComparisonCommand()
        val state = ComparisonState()
        val contribution = createResource(
            id = command.contributions.first()
        )

        every { resourceRepository.findById(contribution.id) } returns Optional.of(contribution)

        assertThrows<ContributionNotFound> { contributionValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(contribution.id) }
    }

    @Test
    fun `Given a comparison create command, when less than two contributions are specified, it throws an exception`() {
        val command = dummyCreateComparisonCommand().copy(
            contributions = listOf(ThingId("R12"))
        )
        val state = ComparisonState()

        assertThrows<RequiresAtLeastTwoContributions> { contributionValidator(command, state) }
    }
}
