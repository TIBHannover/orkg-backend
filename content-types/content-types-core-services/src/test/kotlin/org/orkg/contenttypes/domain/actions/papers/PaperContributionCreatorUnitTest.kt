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
import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand
import org.orkg.contenttypes.input.testing.fixtures.from
import org.orkg.graph.testing.fixtures.createResource

internal class PaperContributionCreatorUnitTest : MockkBaseTest {
    private val contributionCreator: ContributionCreator = mockk()

    private val paperContributionCreator = PaperContributionCreator(contributionCreator)

    @Test
    fun `Given a paper create command, when creating its contents, it returns success`() {
        val command = createPaperCommand()
        val state = CreatePaperState(
            validationCache = mapOf(
                "#temp1" from command,
                "R3003" to Either.right(createResource())
            ),
            bakedStatements = setOf(
                BakedStatement("^0", "#temp1", "R3003")
            ),
            paperId = ThingId("R15632")
        )

        every {
            contributionCreator.create(
                paperId = state.paperId!!,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = command.contents!!,
                contributionCommands = command.contents!!.contributions,
                validationCache = state.validationCache,
                bakedStatements = state.bakedStatements
            )
        } returns listOf(ThingId("R456"))

        val result = paperContributionCreator(command, state)

        result.asClue {
            it.validationCache shouldBe state.validationCache
            it.bakedStatements shouldBe state.bakedStatements
            it.authors shouldBe state.authors
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            contributionCreator.create(
                paperId = state.paperId!!,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingsCommand = command.contents!!,
                contributionCommands = command.contents!!.contributions,
                validationCache = state.validationCache,
                bakedStatements = state.bakedStatements
            )
        }
    }
}
