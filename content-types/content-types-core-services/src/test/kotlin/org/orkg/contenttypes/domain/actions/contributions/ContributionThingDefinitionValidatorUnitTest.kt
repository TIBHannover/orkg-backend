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
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.testing.fixtures.dummyCreateContributionCommand
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass

@Nested
class ContributionThingDefinitionValidatorUnitTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val contributionThingDefinitionValidator = ContributionThingDefinitionValidator(thingRepository, classRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(thingRepository, classRepository)
    }

    @Test
    fun `Given a contribution create command, when validating its thing definitions, it returns success`() {
        val command = dummyCreateContributionCommand()
        val state = ContributionState()

        val `class` = createClass(ThingId("R2000"))

        every { thingRepository.findByThingId(`class`.id) } returns Optional.of(`class`)

        val result = contributionThingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds shouldBe mapOf(`class`.id.value to Either.right(`class`))
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findByThingId(`class`.id) }
    }

    @Test
    fun `Given a contribution create command, when no things are defined, it returns success`() {
        val command = CreateContributionCommand(
            contributorId = ContributorId(UUID.randomUUID()),
            paperId = ThingId("R123"),
            extractionMethod = ExtractionMethod.MANUAL,
            resources = emptyMap(),
            literals = emptyMap(),
            predicates = emptyMap(),
            lists = emptyMap(),
            contribution = ContributionDefinition(
                label = "Contribution 1",
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        ContributionDefinition.StatementObjectDefinition("R3003")
                    )
                )
            )
        )
        val state = ContributionState()

        val result = contributionThingDefinitionValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }
    }
}
