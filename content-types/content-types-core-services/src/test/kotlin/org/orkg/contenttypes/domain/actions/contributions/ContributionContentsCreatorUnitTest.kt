package org.orkg.contenttypes.domain.actions.contributions

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.ContributionState
import org.orkg.contenttypes.domain.actions.CreateContributionCommand
import org.orkg.contenttypes.input.CreateContributionCommandPart
import org.orkg.contenttypes.input.CreatePredicateCommandPart
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.graph.domain.ExtractionMethod
import org.orkg.graph.testing.fixtures.createResource
import java.util.UUID

internal class ContributionContentsCreatorUnitTest : MockkBaseTest {
    private val contributionCreator: ContributionCreator = mockk()

    private val contributionContentsCreatorCreator = ContributionContentsCreator(contributionCreator)

    @Test
    fun `Given a contribution create command, when creating its contents, it returns success`() {
        val command = CreateContributionCommand(
            paperId = ThingId("R15632"),
            contributorId = ContributorId(UUID.randomUUID()),
            extractionMethod = ExtractionMethod.MANUAL,
            predicates = mapOf(
                "#temp1" to CreatePredicateCommandPart(
                    label = "hasResult"
                )
            ),
            contribution = CreateContributionCommandPart(
                label = "Contribution 1",
                statements = mapOf(
                    "#temp1" to listOf(
                        CreateContributionCommandPart.StatementObject("R3003")
                    )
                )
            )
        )
        val state = ContributionState(
            validationCache = mapOf(
                "#temp1" from command,
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
                thingsCommand = command,
                contributionCommands = listOf(command.contribution),
                validationCache = state.validationCache,
                bakedStatements = state.bakedStatements
            )
        } returns listOf(contributionId)

        val result = contributionContentsCreatorCreator(command, state)

        result.asClue {
            it.validationCache shouldBe state.validationCache
            it.bakedStatements shouldBe state.bakedStatements
            it.contributionId shouldBe contributionId
        }

        verify(exactly = 1) {
            contributionCreator.create(
                paperId = command.paperId,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = command,
                contributionCommands = listOf(command.contribution),
                validationCache = state.validationCache,
                bakedStatements = state.bakedStatements
            )
        }
    }
}
