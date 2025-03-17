package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionValidator
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

internal class PaperContributionValidatorUnitTest : MockkBaseTest {
    private val contributionValidator: ContributionValidator = mockk()

    private val paperContributionValidator = PaperContributionValidator(contributionValidator)

    @Test
    fun `Given a paper create command, when validating, it returns success`() {
        val command = createPaperCommand()
        val resource = createResource(id = ThingId("R3003"))
        val state = CreatePaperState(
            tempIds = setOf("#temp1", "#temp2", "#temp3", "#temp4"),
            validatedIds = mapOf(
                "R3003" to Either.right(resource),
                "#temp1" to Either.left("#temp1"),
                "#temp2" to Either.left("#temp2"),
                "#temp3" to Either.left("#temp3"),
                "#temp4" to Either.left("#temp4")
            )
        )
        val bakedStatements = setOf(
            BakedStatement("^0", Predicates.hasResearchProblem.value, "R3003"),
            BakedStatement("^0", Predicates.hasEvaluation.value, "#temp1"),
            BakedStatement("^0", Predicates.hasEvaluation.value, "R3004"),
            BakedStatement("R3004", "#temp3", "R3003"),
            BakedStatement("R3004", "#temp3", "#temp2"),
            BakedStatement("R3004", "#temp4", "#temp1")
        )
        val template = createClass(ThingId("C123"))
        val `class` = createClass(ThingId("R3004"))
        val hasResearchProblem = createPredicate(Predicates.hasResearchProblem)
        val hasEvaluation = createPredicate(Predicates.hasEvaluation)
        val validatedIds = state.validatedIds + mapOf<String, Either<String, Thing>>(
            "C123" to Either.right(template),
            "R3003" to Either.right(resource),
            "R3004" to Either.right(`class`),
            Predicates.hasEvaluation.value to Either.right(hasEvaluation),
            Predicates.hasResearchProblem.value to Either.right(hasResearchProblem)
        )

        every {
            contributionValidator.validate(
                validatedIdsIn = state.validatedIds,
                tempIds = state.tempIds,
                thingDefinitions = command.contents!!,
                contributionDefinitions = command.contents!!.contributions
            )
        } returns ContributionValidator.Result(validatedIds, bakedStatements)

        val result = paperContributionValidator(command, state)

        result.asClue {
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds + validatedIds
            it.bakedStatements shouldBe bakedStatements
            it.paperId shouldBe null
        }

        verify(exactly = 1) {
            contributionValidator.validate(
                validatedIdsIn = state.validatedIds,
                tempIds = state.tempIds,
                thingDefinitions = command.contents!!,
                contributionDefinitions = command.contents!!.contributions
            )
        }
    }

    @Test
    fun `Given a paper create command, when contents are null, it does nothing`() {
        val command = createPaperCommand().copy(contents = null)
        val state = CreatePaperState()

        val result = paperContributionValidator(command, state)

        result.asClue {
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.bakedStatements shouldBe state.bakedStatements
            it.paperId shouldBe null
        }
    }
}
