package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.PublishLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishLiteratureListCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.StatementUseCases

internal class LiteratureListVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val statementService: StatementUseCases = mockk()

    private val literatureListVersionHistoryUpdater = LiteratureListVersionHistoryUpdater(statementService)

    @Test
    fun `Given a literature list publish command, it crates a new previous version statement`() {
        val literatureList = createLiteratureList()
        val command = dummyPublishLiteratureListCommand().copy(id = literatureList.id)
        val literatureListVersionId = ThingId("R165")
        val state = PublishLiteratureListState(literatureList, literatureListVersionId)

        every {
            statementService.add(
                userId = command.contributorId,
                subject = literatureList.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = literatureListVersionId
            )
        } just runs

        literatureListVersionHistoryUpdater(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.literatureListVersionId shouldBe literatureListVersionId
        }

        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = literatureList.id,
                predicate = Predicates.hasPublishedVersion,
                `object` = literatureListVersionId
            )
        }
    }
}
