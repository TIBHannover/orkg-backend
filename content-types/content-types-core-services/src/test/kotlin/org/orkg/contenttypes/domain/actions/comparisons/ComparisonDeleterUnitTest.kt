package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonInUse
import org.orkg.contenttypes.domain.actions.AbstractGraphDeleter
import org.orkg.contenttypes.domain.actions.DeleteComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.deleteComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class ComparisonDeleterUnitTest : MockkBaseTest {
    private val abstractGraphDeleter: AbstractGraphDeleter = mockk()

    private val comparisonDeleter = ComparisonDeleter(abstractGraphDeleter)

    @Test
    fun `Given a comparison delete command, when deleting, it returns success`() {
        val comparison = createResource(classes = setOf(Classes.comparison))
        val command = deleteComparisonCommand().copy(comparisonId = comparison.id)
        val statements = listOf(createStatement())
        val state = DeleteComparisonState().copy(
            comparison = comparison,
            statements = statements.groupBy { it.subject.id },
        )

        every { abstractGraphDeleter.delete(any(), comparison, statements, command.contributorId) } returns true

        comparisonDeleter(command, state) shouldBe state

        verify(exactly = 1) { abstractGraphDeleter.delete(any(), comparison, statements, command.contributorId) }
    }

    @Test
    fun `Given a comparison delete command, when deleting and graph deletion failed, it throws an exception`() {
        val comparison = createResource(classes = setOf(Classes.comparison))
        val command = deleteComparisonCommand().copy(comparisonId = comparison.id)
        val statements = listOf(createStatement())
        val state = DeleteComparisonState().copy(
            comparison = comparison,
            statements = statements.groupBy { it.subject.id },
        )

        every { abstractGraphDeleter.delete(any(), comparison, statements, command.contributorId) } returns false

        shouldThrow<ComparisonInUse> { comparisonDeleter(command, state) }

        verify(exactly = 1) { abstractGraphDeleter.delete(any(), comparison, statements, command.contributorId) }
    }

    @Test
    fun `Given a comparison delete command, when comparison is null, it does nothing`() {
        val comparison = createComparison()
        val command = deleteComparisonCommand().copy(comparisonId = comparison.id)
        val state = DeleteComparisonState()

        comparisonDeleter(command, state) shouldBe state
    }
}
