package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.Either
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.actions.BakedStatement
import org.orkg.contenttypes.domain.actions.ContributionCreator
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.input.testing.fixtures.dummyCreatePaperCommand
import org.orkg.graph.testing.fixtures.createResource

internal class PaperContributionCreatorUnitTest {
    private val contributionCreator: ContributionCreator = mockk()

    private val paperContributionCreator = PaperContributionCreator(contributionCreator)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(contributionCreator)
    }

    @Test
    fun `Given a paper create command, when creating its contents, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = CreatePaperState(
            tempIds = setOf("#temp1"),
            validatedIds = mapOf(
                "#temp1" to Either.left("#temp1"),
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
                thingDefinitions = command.contents!!,
                contributionDefinitions = command.contents!!.contributions,
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            )
        } returns listOf(ThingId("R456"))

        val result = paperContributionCreator(command, state)

        result.asClue {
            it.tempIds shouldBe state.tempIds
            it.validatedIds shouldBe state.validatedIds
            it.bakedStatements shouldBe state.bakedStatements
            it.authors shouldBe state.authors
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            contributionCreator.create(
                paperId = state.paperId!!,
                contributorId = command.contributorId,
                extractionMethod = command.extractionMethod,
                thingDefinitions = command.contents!!,
                contributionDefinitions = command.contents!!.contributions,
                validatedIds = state.validatedIds,
                bakedStatements = state.bakedStatements
            )
        }
    }
}
