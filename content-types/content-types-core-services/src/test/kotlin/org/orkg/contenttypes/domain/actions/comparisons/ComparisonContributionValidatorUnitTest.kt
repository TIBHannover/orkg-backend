package org.orkg.contenttypes.domain.actions.comparisons

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.ContributionNotFound
import org.orkg.contenttypes.domain.RequiresAtLeastTwoContributions
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource

class ComparisonContributionValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()

    private val contributionValidator = ComparisonContributionValidator<List<ThingId>?, Unit>(resourceRepository) { it }

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository)
    }

    @Test
    fun `Given a list of contributions, when validating its contributions, it returns success`() {
        val command = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120"))
        val contribution = createResource(classes = setOf(Classes.contribution))

        every { resourceRepository.findById(any()) } returns Optional.of(contribution)

        contributionValidator(command, Unit)

        command.forEach {
            verify(exactly = 1) { resourceRepository.findById(it) }
        }
    }

    @Test
    fun `Given a list of contributions, when contribution is missing, it throws an exception`() {
        val command = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120"))

        every { resourceRepository.findById(command.first()) } returns Optional.empty()

        assertThrows<ContributionNotFound> { contributionValidator(command, Unit) }

        verify(exactly = 1) { resourceRepository.findById(command.first()) }
    }

    @Test
    fun `Given a list of contributions, when resource its not a contribution, it throws an exception`() {
        val command = listOf(ThingId("R6541"), ThingId("R5364"), ThingId("R9786"), ThingId("R3120"))
        val contribution = createResource(id = command.first())

        every { resourceRepository.findById(contribution.id) } returns Optional.of(contribution)

        assertThrows<ContributionNotFound> { contributionValidator(command, Unit) }

        verify(exactly = 1) { resourceRepository.findById(contribution.id) }
    }

    @Test
    fun `Given a list of contributions, when less than two contributions are specified, it throws an exception`() {
        assertThrows<RequiresAtLeastTwoContributions> { contributionValidator(listOf(ThingId("R12")), Unit) }
    }

    @Test
    fun `Given a list of contributions, when null, it returns success`() {
        assertDoesNotThrow { contributionValidator(null, Unit) }
    }
}
