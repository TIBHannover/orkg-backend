package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreatePaperUseCase
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import java.util.Optional

internal class PaperContributionValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()

    private val paperContributionValidator = PaperContributionValidator(thingRepository)

    @Test
    fun `Given a paper create command, when validating its contributions, it returns success`() {
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

        val template = createClass(ThingId("C123"))
        val `class` = createClass(ThingId("R3004"))
        val hasResearchProblem = createPredicate(Predicates.hasResearchProblem)
        val hasEvaluation = createPredicate(Predicates.hasEvaluation)

        every { thingRepository.findById(`class`.id) } returns Optional.of(`class`)
        every { thingRepository.findById(template.id) } returns Optional.of(template)
        every { thingRepository.findById(Predicates.hasResearchProblem) } returns Optional.of(hasResearchProblem)
        every { thingRepository.findById(Predicates.hasEvaluation) } returns Optional.of(hasEvaluation)

        val result = paperContributionValidator(command, state)

        val expectedStatements = setOf(
            BakedStatement("^0", Predicates.hasResearchProblem.value, "R3003"),
            BakedStatement("^0", Predicates.hasEvaluation.value, "#temp1"),
            BakedStatement("^1", Predicates.hasResearchProblem.value, "R3003"),
            BakedStatement("^1", Predicates.hasEvaluation.value, "#temp1"),
            BakedStatement("^1", Predicates.hasEvaluation.value, "R3004"),
            BakedStatement("R3004", "#temp3", "R3003"),
            BakedStatement("R3004", "#temp3", "#temp2"),
            BakedStatement("R3004", "#temp4", "#temp1")
        )
        val expectedTempIds = state.tempIds
        val expectedValidatedIds = state.validatedIds + mapOf(
            "C123" to Either.right(template),
            "R3003" to Either.right(resource),
            "R3004" to Either.right(`class`),
            Predicates.hasEvaluation.value to Either.right(hasEvaluation),
            Predicates.hasResearchProblem.value to Either.right(hasResearchProblem)
        )

        result.asClue {
            it.tempIds shouldBe expectedTempIds
            it.validatedIds shouldBe expectedValidatedIds
            it.bakedStatements shouldBe expectedStatements
            it.authors.size shouldBe 0
            it.paperId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findById(`class`.id) }
        verify(exactly = 1) { thingRepository.findById(template.id) }
        verify(exactly = 1) { thingRepository.findById(Predicates.hasResearchProblem) }
        verify(exactly = 1) { thingRepository.findById(Predicates.hasEvaluation) }
    }

    @Test
    fun `Given a paper create command, when class of contribution does not exist, it throws an exception`() {
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
        val template = createClass(ThingId("C123"))

        every { thingRepository.findById(template.id) } returns Optional.empty()

        assertThrows<ThingNotFound> { paperContributionValidator(command, state) }

        verify(exactly = 1) { thingRepository.findById(template.id) }
    }

    @Test
    fun `Given a paper create command, when class of contribution is not a class, it throws an exception`() {
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
        val template = createResource(id = ThingId("C123"))

        every { thingRepository.findById(template.id) } returns Optional.of(template)

        assertThrows<ThingIsNotAClass> { paperContributionValidator(command, state) }

        verify(exactly = 1) { thingRepository.findById(template.id) }
    }

    @Test
    fun `Given a paper create command, when contribution does not contain any statements, it throws an exception`() {
        val command = createPaperCommand().copy(
            contents = CreatePaperUseCase.CreateCommand.PaperContents(
                contributions = listOf(
                    CreateContributionCommandPart(
                        label = "Contribution",
                        statements = emptyMap()
                    )
                )
            )
        )
        val state = CreatePaperState()
        val exception = EmptyContribution()

        assertThrows<EmptyContribution> {
            paperContributionValidator(command, state)
        }.asClue {
            it.message shouldBe exception.message
        }
    }
}
