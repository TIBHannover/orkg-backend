package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.PublishLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.publishLiteratureListCommand
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.UnsafeStatementUseCases

internal class LiteratureListVersionHistoryUpdaterUnitTest : MockkBaseTest {
    private val unsafeStatementUseCases: UnsafeStatementUseCases = mockk()

    private val literatureListVersionHistoryUpdater = LiteratureListVersionHistoryUpdater(unsafeStatementUseCases)

    @Test
    fun `Given a literature list publish command, it crates a new previous version statement`() {
        val literatureList = createLiteratureList()
        val command = publishLiteratureListCommand().copy(id = literatureList.id)
        val literatureListVersionId = ThingId("R165")
        val state = PublishLiteratureListState(literatureList, literatureListVersionId)

        every {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = literatureList.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = literatureListVersionId
                )
            )
        } returns StatementId("S1")

        literatureListVersionHistoryUpdater(command, state).asClue {
            it.literatureList shouldBe literatureList
            it.literatureListVersionId shouldBe literatureListVersionId
        }

        verify(exactly = 1) {
            unsafeStatementUseCases.create(
                CreateStatementUseCase.CreateCommand(
                    contributorId = command.contributorId,
                    subjectId = literatureList.id,
                    predicateId = Predicates.hasPublishedVersion,
                    objectId = literatureListVersionId
                )
            )
        }
    }
}
