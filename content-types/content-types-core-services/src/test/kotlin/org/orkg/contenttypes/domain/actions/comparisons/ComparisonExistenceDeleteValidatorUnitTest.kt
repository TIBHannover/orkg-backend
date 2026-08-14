package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonService
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.actions.DeleteComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.deleteComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.Optional

internal class ComparisonExistenceDeleteValidatorUnitTest : MockkBaseTest {
    private val comparisonService: ComparisonService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val comparisonExistenceDeleteValidator = ComparisonExistenceDeleteValidator(comparisonService, resourceRepository)

    @Test
    fun `Given a comparison delete command, when checking for comparison existence, it returns success`() {
        val comparison = createComparison()
        val command = deleteComparisonCommand().copy(comparisonId = comparison.id)
        val state = DeleteComparisonState()
        val root = createResource(
            id = comparison.id,
            label = comparison.title,
            classes = setOf(Classes.comparison),
        )
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        every { resourceRepository.findById(comparison.id) } returns Optional.of(root)
        every { comparisonService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)

        comparisonExistenceDeleteValidator(command, state).asClue {
            it.comparison shouldBe root
            it.statements shouldBe statements
        }

        verify(exactly = 1) { resourceRepository.findById(comparison.id) }
        verify(exactly = 1) { comparisonService.findSubgraph(root) }
    }

    @Test
    fun `Given a comparison delete command, when checking for comparison existence and comparison is not found, it does not throw an exception`() {
        val comparison = createComparison()
        val command = deleteComparisonCommand().copy(comparisonId = comparison.id)
        val state = DeleteComparisonState()

        every { resourceRepository.findById(comparison.id) } returns Optional.empty()

        comparisonExistenceDeleteValidator(command, state) shouldBe state

        verify(exactly = 1) { resourceRepository.findById(comparison.id) }
    }
}
