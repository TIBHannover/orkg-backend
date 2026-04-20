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
import org.orkg.contenttypes.domain.ComparisonPath
import org.orkg.contenttypes.domain.LabeledComparisonPath
import org.orkg.contenttypes.domain.actions.CreateComparisonState
import org.orkg.contenttypes.domain.testing.fixtures.createLabeledComparisonPaths
import org.orkg.contenttypes.input.ComparisonTableUseCases
import org.orkg.contenttypes.input.testing.fixtures.createComparisonCommand
import org.orkg.contenttypes.output.ComparisonTableRepository
import org.orkg.graph.domain.Predicates

internal class ComparisonTableInitializerUnitTest : MockkBaseTest {
    private val comparisonTableUseCases: ComparisonTableUseCases = mockk()
    private val comparisonTableRepository: ComparisonTableRepository = mockk()

    private val comparisonTableInitializer = ComparisonTableInitializer(comparisonTableUseCases, comparisonTableRepository)

    @Test
    fun `Given a comparison create command, it crates a new comparison table with all available path`() {
        val command = createComparisonCommand()
        val comparisonId = ThingId("R123")
        val state = CreateComparisonState(comparisonId = comparisonId)
        val expectedSelectedPaths = listOf(
            LabeledComparisonPath(
                id = Predicates.addresses,
                label = "addresses",
                description = "addresses",
                type = ComparisonPath.Type.PREDICATE,
                sources = 2,
                children = emptyList(),
            ),
            LabeledComparisonPath(
                id = Predicates.mentions,
                label = "mentions",
                description = null,
                type = ComparisonPath.Type.PREDICATE,
                sources = 2,
                children = emptyList(),
            ),
            LabeledComparisonPath(
                id = ThingId("R21325"),
                label = "Dummy Rosetta Stone Template Label",
                description = "Some description about the rosetta stone template",
                type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT,
                sources = 1,
                children = listOf(
                    LabeledComparisonPath(
                        id = Predicates.hasSubjectPosition,
                        label = "resource property placeholder",
                        description = "resource property description",
                        type = ComparisonPath.Type.ROSETTA_STONE_STATEMENT_VALUE,
                        sources = 2,
                        children = emptyList(),
                    ),
                ),
            ),
        )

        every { comparisonTableUseCases.findAllPathsByComparisonId(state.comparisonId!!) } returns createLabeledComparisonPaths()
        every { comparisonTableRepository.save(any()) } just runs

        val result = comparisonTableInitializer(command, state)
        result shouldBe state

        verify(exactly = 1) { comparisonTableUseCases.findAllPathsByComparisonId(state.comparisonId!!) }
        verify(exactly = 1) {
            comparisonTableRepository.save(
                withArg {
                    it.comparisonId shouldBe state.comparisonId
                    it.selectedPaths shouldBe expectedSelectedPaths
                },
            )
        }
    }
}
