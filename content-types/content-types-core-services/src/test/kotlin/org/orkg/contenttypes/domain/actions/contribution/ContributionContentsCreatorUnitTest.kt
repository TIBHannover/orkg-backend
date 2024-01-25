package org.orkg.contenttypes.domain.actions.contribution

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
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.input.ContributionDefinition
import org.orkg.contenttypes.input.PredicateDefinition
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.testing.fixtures.createResource

class ContributionContentsCreatorUnitTest {
    private val contributionCreator: ContributionCreator = mockk()

    private val contributionContentsCreatorCreator = ContributionContentsCreator(contributionCreator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(contributionCreator)
    }

    @Test
    fun `Given a contribution create command, when creating its contents, it returns success`() {
        val command = CreateContributionCommand(
            paperId = ThingId("R15632"),
            contributorId = ContributorId(UUID.randomUUID()),
            extractionMethod = ExtractionMethod.MANUAL,
            predicates = mapOf(
                "#temp1" to PredicateDefinition(
                    label = "hasResult"
                )
            ),
            contribution = ContributionDefinition(
                label = "Contribution 1",
                statements = mapOf(
                    "#temp1" to listOf(
                        ContributionDefinition.StatementObjectDefinition("R3003")
                    )
                )
            )
        )
        val state = ContributionState(
            tempIds = setOf("#temp1"),
            validatedIds = mapOf(
                "#temp1" to Either.left("#temp1"),
                "R3003" to Either.right(createResource())
            ),
            bakedStatements = setOf(
                BakedStatement("^0", "#temp1", "R3003")
            )
        )
        val contributionId = ThingId("R456")

        every {
            contributionCreator.create(
                paperId = command.paperId,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = command,
                contributionDefinitions = listOf(command.contribution),
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            )
        } returns listOf(contributionId)

        val result = contributionContentsCreatorCreator(command, state)

        result.asClue {
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.bakedStatements shouldBe state.bakedStatements
            it.contributionId shouldBe contributionId
        }

        verify(exactly = 1) {
            contributionCreator.create(
                paperId = command.paperId,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = command,
                contributionDefinitions = listOf(command.contribution),
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            )
        }
    }
}
