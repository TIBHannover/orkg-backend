package org.orkg.contenttypes.domain.actions.comparisons

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.stream.Stream
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ComparisonTable
import org.orkg.contenttypes.domain.actions.UpdateComparisonCommand
import org.orkg.contenttypes.domain.actions.UpdateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createComparison
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateComparisonCommand
import org.orkg.contenttypes.output.ComparisonTableRepository

internal class ComparisonTableUpdaterUnitTest : MockkBaseTest {
    private val comparisonTableRepository: ComparisonTableRepository = mockk()

    private val comparisonTableUpdater = ComparisonTableUpdater(comparisonTableRepository)

    @Test
    fun `Given a comparison update command, when comparison config and data are unchanged, does nothing`() {
        val comparison = createComparison()
        val command = dummyUpdateComparisonCommand().copy(
            comparisonId = comparison.id,
            config = comparison.config,
            data = comparison.data
        )
        val state = UpdateComparisonState(comparison)

        comparisonTableUpdater(command, state).asClue {
            it.comparison shouldBe comparison
            it.authors shouldBe state.authors
        }
    }

    @ParameterizedTest
    @MethodSource("validUpdateCommands")
    fun `Given a comparison update command, when comparison config or data is set, it updates the comparison table`(value: UpdateComparisonCommand) {
        val comparison = createComparison()
        val command = value.copy(comparisonId = comparison.id)
        val state = UpdateComparisonState(comparison)

        every { comparisonTableRepository.update(any()) } just runs

        comparisonTableUpdater(command, state).asClue {
            it.comparison shouldBe comparison
            it.authors shouldBe state.authors
        }

        verify(exactly = 1) {
            comparisonTableRepository.update(
                ComparisonTable(
                    id = state.comparison!!.id,
                    config = command.config ?: state.comparison!!.config,
                    data = command.data ?: state.comparison!!.data
                )
            )
        }
    }

    @Test
    fun `Given a comparison update command, when comparison config and data is not set, does nothing`() {
        val comparison = createComparison()
        val command = dummyUpdateComparisonCommand().copy(
            comparisonId = comparison.id,
            config = null,
            data = null
        )
        val state = UpdateComparisonState(comparison)

        comparisonTableUpdater(command, state).asClue {
            it.comparison shouldBe comparison
            it.authors shouldBe state.authors
        }
    }

    companion object {
        @JvmStatic
        fun validUpdateCommands(): Stream<Arguments> = Stream.of(
            dummyUpdateComparisonCommand().let { command ->
                command.copy(
                    data = command.data!!.copy(predicates = emptyList())
                )
            },
            dummyUpdateComparisonCommand().let { command ->
                command.copy(
                    config = command.config!!.copy(contributions = emptyList())
                )
           },
            dummyUpdateComparisonCommand().let { command ->
                command.copy(
                    config = null,
                    data = command.data!!.copy(predicates = emptyList())
                )
            },
            dummyUpdateComparisonCommand().let { command ->
                command.copy(
                    config = command.config!!.copy(contributions = emptyList()),
                    data = null
                )
            }
        ).map { Arguments.of(it) }
    }
}
