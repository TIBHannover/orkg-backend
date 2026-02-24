package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.PublishComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparisonTable
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.input.testing.fixtures.publishComparisonCommand
import org.orkg.contenttypes.output.ComparisonTableRepository
import java.util.Optional

internal class ComparisonVersionTableCreatorUnitTest : MockkBaseTest {
    private val comparisonTableUseCases: ComparisonTableUseCases = mockk()
    private val comparisonTableRepository: ComparisonTableRepository = mockk()

    private val comparisonVersionTableCreator = ComparisonVersionTableCreator(comparisonTableUseCases, comparisonTableRepository)

    @Test
    fun `Given a comparison publish command, it saves a new comparison table for the created comparison version`() {
        val command = publishComparisonCommand()
        val state = PublishComparisonState(comparisonVersionId = ThingId("R1235"))
        val table = createComparisonTable().copy(comparisonId = command.id)

        every { comparisonTableUseCases.findByComparisonId(command.id) } returns Optional.of(table)
        every { comparisonTableRepository.save(any()) } just runs

        comparisonVersionTableCreator(command, state)

        verify(exactly = 1) { comparisonTableUseCases.findByComparisonId(command.id) }
        verify(exactly = 1) {
            comparisonTableRepository.save(
                withArg {
                    it.comparisonId shouldBe state.comparisonVersionId!!
                    it.selectedPaths shouldBe table.selectedPaths
                    it.titles shouldBe table.titles
                    it.subtitles shouldBe table.subtitles
                    it.values shouldBe table.values
                }
            )
        }
    }

    @Test
    fun `Given a comparison publish command, when comparison table service returns no table, it saves a new empty comparison table for the created comparison version`() {
        val command = publishComparisonCommand()
        val state = PublishComparisonState(comparisonVersionId = ThingId("R1235"))

        every { comparisonTableUseCases.findByComparisonId(command.id) } returns Optional.empty()
        every { comparisonTableRepository.save(any()) } just runs

        comparisonVersionTableCreator(command, state)

        verify(exactly = 1) { comparisonTableUseCases.findByComparisonId(command.id) }
        verify(exactly = 1) {
            comparisonTableRepository.save(
                withArg {
                    it.comparisonId shouldBe state.comparisonVersionId!!
                    it.selectedPaths shouldBe emptyList()
                    it.titles shouldBe emptyList()
                    it.subtitles shouldBe emptyList()
                    it.values shouldBe emptyMap()
                }
            )
        }
    }
}
