package org.orkg.contenttypes.domain.actions.contributions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.EmptyContribution
import org.orkg.contenttypes.domain.ThingIsNotAClass
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.CreateContributionUseCase
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateContributionCommand
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.ThingNotFound
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource

internal class ContributionContentsValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()

    private val contributionContentsValidator = ContributionContentsValidator(thingRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository)
    }

    @Test
    fun `Given a contribution create command, when validating, it returns success`() {
        val command = dummyCreateContributionCommand()
        val resource = createResource(id = ThingId("R3003"))
        val state = ContributionState(
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

        every { thingRepository.findByThingId(`class`.id) } returns Optional.of(`class`)
        every { thingRepository.findByThingId(template.id) } returns Optional.of(template)
        every { thingRepository.findByThingId(Predicates.hasResearchProblem) } returns Optional.of(hasResearchProblem)
        every { thingRepository.findByThingId(Predicates.hasEvaluation) } returns Optional.of(hasEvaluation)

        val result = contributionContentsValidator(command, state)

        val expectedStatements = setOf(
            BakedStatement("^0", Predicates.hasResearchProblem.value, "R3003"),
            BakedStatement("^0", Predicates.hasEvaluation.value, "#temp1"),
            BakedStatement("^0", Predicates.hasEvaluation.value, "R3004"),
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
            it.contributionId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findByThingId(`class`.id) }
        verify(exactly = 1) { thingRepository.findByThingId(template.id) }
        verify(exactly = 1) { thingRepository.findByThingId(Predicates.hasResearchProblem) }
        verify(exactly = 1) { thingRepository.findByThingId(Predicates.hasEvaluation) }
    }

    @Test
    fun `Given a contribution create command, when class of contribution does not exist, it throws an exception`() {
        val command = dummyCreateContributionCommand()
        val resource = createResource(id = ThingId("R3003"))
        val state = ContributionState(
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

        every { thingRepository.findByThingId(template.id) } returns Optional.empty()

        assertThrows<ThingNotFound> { contributionContentsValidator(command, state) }

        verify(exactly = 1) { thingRepository.findByThingId(template.id) }
    }

    @Test
    fun `Given a contribution create command, when class of contribution is not a class, it throws an exception`() {
        val command = dummyCreateContributionCommand()
        val resource = createResource(id = ThingId("R3003"))
        val state = ContributionState(
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

        every { thingRepository.findByThingId(template.id) } returns Optional.of(template)

        assertThrows<ThingIsNotAClass> { contributionContentsValidator(command, state) }

        verify(exactly = 1) { thingRepository.findByThingId(template.id) }
    }

    @Test
    fun `Given a contribution create command, when contribution does not contain any statements, it throws an exception`() {
        val command = CreateContributionUseCase.CreateCommand(
            contributorId = ContributorId(UUID.randomUUID()),
            extractionMethod = ExtractionMethod.MANUAL,
            paperId = ThingId("R123"),
            contribution = ContributionDefinition(
                label = "Contribution",
                statements = emptyMap()
            )
        )
        val state = ContributionState()
        val exception = EmptyContribution()

        assertThrows<EmptyContribution> {
            contributionContentsValidator(command, state)
        }.asClue {
            it.message shouldBe exception.message
        }
    }
}
