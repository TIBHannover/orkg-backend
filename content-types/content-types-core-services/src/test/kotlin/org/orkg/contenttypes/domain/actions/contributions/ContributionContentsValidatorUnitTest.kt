package org.orkg.contenttypes.domain.actions.contributions

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
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.ContributionValidator
import org.orkg.contenttypes.input.CreateThingCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createContributionCommand
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Thing
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

internal class ContributionContentsValidatorUnitTest : MockkBaseTest {
    private val contributionValidator: ContributionValidator = mockk()

    private val contributionContentsValidator = ContributionContentsValidator(contributionValidator)

    @Test
    fun `Given a contribution create command, when validating, it returns success`() {
        val command = createContributionCommand()
        val resource = createResource(id = ThingId("R3003"))
        val state = ContributionState(
            validationCache = mapOf(
                "R3003" to Either.right(resource),
                "#temp1" from command,
                "#temp2" from command,
                "#temp3" from command,
                "#temp4" from command
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
        val validationCache = state.validationCache + mapOf<String, Either<CreateThingCommandPart, Thing>>(
            "C123" to Either.right(template),
            "R3003" to Either.right(resource),
            "R3004" to Either.right(`class`),
            Predicates.hasEvaluation.value to Either.right(hasEvaluation),
            Predicates.hasResearchProblem.value to Either.right(hasResearchProblem)
        )

        every {
            contributionValidator.validate(
                validationCacheIn = state.validationCache.toMutableMap(),
                thingCommands = command.all(),
                contributionCommands = listOf(command.contribution)
            )
        } returns ContributionValidator.Result(validationCache, bakedStatements)

        val result = contributionContentsValidator(command, state)

        result.asClue {
            it.validationCache shouldBe validationCache
            it.bakedStatements shouldBe bakedStatements
            it.contributionId shouldBe null
        }

        verify(exactly = 1) {
            contributionValidator.validate(
                validationCacheIn = state.validationCache.toMutableMap(),
                thingCommands = command.all(),
                contributionCommands = listOf(command.contribution)
            )
        }
    }
}
