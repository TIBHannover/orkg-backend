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
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createLabeledComparisonPaths
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.input.testing.fixtures.createComparisonCommand
import org.orkg.contenttypes.output.ComparisonTableRepository

internal class ComparisonTableInitializerUnitTest : MockkBaseTest {
    private val comparisonTableUseCases: ComparisonTableUseCases = mockk()
    private val comparisonTableRepository: ComparisonTableRepository = mockk()

    private val comparisonTableInitializer = ComparisonTableInitializer(comparisonTableUseCases, comparisonTableRepository)

    @Test
    fun `Given a comparison create command, it crates a new comparison table with all available path`() {
        val command = createComparisonCommand()
        val comparisonId = ThingId("R123")
        val state = CreateComparisonState(comparisonId = comparisonId)
        val selectedPaths = createLabeledComparisonPaths()

        every { comparisonTableUseCases.findAllPathsByComparisonId(state.comparisonId!!) } returns selectedPaths
        every { comparisonTableRepository.save(any()) } just runs

        val result = comparisonTableInitializer(command, state)
        result shouldBe state

        verify(exactly = 1) { comparisonTableUseCases.findAllPathsByComparisonId(state.comparisonId!!) }
        verify(exactly = 1) {
            comparisonTableRepository.save(
                withArg {
                    it.comparisonId shouldBe state.comparisonId
                    it.selectedPaths shouldBe selectedPaths
                },
            )
        }
    }
}
