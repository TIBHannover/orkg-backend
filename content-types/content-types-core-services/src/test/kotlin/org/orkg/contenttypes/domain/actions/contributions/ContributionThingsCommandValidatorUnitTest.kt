package org.orkg.contenttypes.domain.actions.contributions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.testing.fixtures.createContributionCommand
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ThingRepository
import org.orkg.graph.testing.fixtures.createClass
import java.util.Optional
import java.util.UUID

@Nested
internal class ContributionThingsCommandValidatorUnitTest : MockkBaseTest {
    private val thingRepository: ThingRepository = mockk()
    private val classRepository: ClassRepository = mockk()

    private val contributionThingsCommandValidator = ContributionThingsCommandValidator(thingRepository, classRepository)

    @Test
    fun `Given a contribution create command, when validating its thing commands, it returns success`() {
        val command = createContributionCommand()
        val state = ContributionState()

        val `class` = createClass(ThingId("R2000"))

        every { thingRepository.findById(`class`.id) } returns Optional.of(`class`)

        val result = contributionThingsCommandValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds shouldBe mapOf(`class`.id.value to Either.right(`class`))
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }

        verify(exactly = 1) { thingRepository.findById(`class`.id) }
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
            contribution = CreateContributionCommandPart(
                label = "Contribution 1",
                statements = mapOf(
                    Predicates.hasResearchProblem.value to listOf(
                        CreateContributionCommandPart.StatementObject("R3003")
                    )
                )
            )
        )
        val state = ContributionState()

        val result = contributionThingsCommandValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.contributionId shouldBe null
        }
    }
}
