package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.Comparison
import org.orkg.contenttypes.domain.ComparisonNotFound
import org.orkg.contenttypes.domain.ComparisonNotModifiable
import org.orkg.contenttypes.domain.ComparisonService
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.HeadVersion
import org.orkg.contenttypes.domain.VersionInfo
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.updateComparisonCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.Optional

internal class ComparisonExistenceValidatorUnitTest : MockkBaseTest {
    private val comparisonService: ComparisonService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val comparisonExistenceValidator = ComparisonExistenceValidator(comparisonService, resourceRepository)

    @Test
    fun `Given a comparison update command, when checking for comparison existence, it returns success`() {
        val comparison = createComparison()
        val command = updateComparisonCommand().copy(comparisonId = comparison.id)
        val state = UpdateComparisonState()
        val root = createResource(
            id = comparison.id,
            label = comparison.title,
            classes = setOf(Classes.comparison)
        )
        val statements = listOf(createStatement(subject = root)).groupBy { it.subject.id }
        val table = ComparisonTable.empty(root.id)
        val versionInfo = VersionInfo(
            head = HeadVersion(root),
            published = emptyList()
        )

        mockkObject(Comparison.Companion) {
            every { resourceRepository.findById(comparison.id) } returns Optional.of(root)
            every { comparisonService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { with(comparisonService) { root.findTableData() } } returns table
            every { with(comparisonService) { root.findVersionInfo(statements) } } returns versionInfo
            every { Comparison.from(root, statements, table, versionInfo) } returns comparison

            comparisonExistenceValidator(command, state).asClue {
                it.comparison shouldBe comparison
                it.statements shouldBe statements
                it.authors shouldBe state.authors
            }

            verify(exactly = 1) { resourceRepository.findById(comparison.id) }
            verify(exactly = 1) { comparisonService.findSubgraph(root) }
            verify(exactly = 1) { with(comparisonService) { root.findTableData() } }
            verify(exactly = 1) { with(comparisonService) { root.findVersionInfo(statements) } }
            verify(exactly = 1) { Comparison.from(root, statements, table, versionInfo) }
        }
    }

    @Test
    fun `Given a comparison update command, when comparison is published, it throws an exception`() {
        val comparisonId = ThingId("R123")
        val command = updateComparisonCommand().copy(comparisonId = comparisonId)
        val root = createResource(id = comparisonId, classes = setOf(Classes.comparisonPublished))
        val state = UpdateComparisonState()

        every { resourceRepository.findById(comparisonId) } returns Optional.of(root)

        assertThrows<ComparisonNotModifiable> { comparisonExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(comparisonId) }
    }

    @Test
    fun `Given a comparison update command, when checking for comparison existence and comparison is not found, it throws an exception`() {
        val comparison = createComparison()
        val command = updateComparisonCommand().copy(comparisonId = comparison.id)
        val state = UpdateComparisonState()

        every { resourceRepository.findById(comparison.id) } returns Optional.empty()

        shouldThrow<ComparisonNotFound> { comparisonExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(comparison.id) }
    }
}
