package org.orkg.contenttypes.domain.actions.papers

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
import org.orkg.contenttypes.domain.actions.CreatePaperState
import org.orkg.contenttypes.domain.actions.PublicationInfoCreator
import org.orkg.contenttypes.input.PublicationInfoCommand
import org.orkg.contenttypes.input.testing.fixtures.createPaperCommand

internal class PaperPublicationInfoCreatorUnitTest : MockkBaseTest {
    private val publicationInfoCreator: PublicationInfoCreator = mockk()

    private val paperPublicationInfoCreator = PaperPublicationInfoCreator(publicationInfoCreator)

    @Test
    fun `Given a paper create command, when linking empty publication info, it does nothing`() {
        val paperId = ThingId("R123")
        val command = createPaperCommand().copy(
            publicationInfo = null
        )
        val state = CreatePaperState(
            paperId = paperId
        )

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.validationCache.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }
    }

    @Test
    fun `Given a paper create command, when publication info, it returns success`() {
        val paperId = ThingId("R123")
        val month = 5
        val command = createPaperCommand().copy(
            publicationInfo = PublicationInfoCommand(
                publishedMonth = month,
                publishedYear = null,
                publishedIn = null,
                url = null
            )
        )
        val state = CreatePaperState(
            paperId = paperId
        )

        every {
            publicationInfoCreator.create(
                contributorId = command.contributorId,
                subjectId = state.paperId!!,
                publicationInfo = command.publicationInfo!!
            )
        } just runs

        val result = paperPublicationInfoCreator(command, state)

        result.asClue {
            it.validationCache.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors.size shouldBe 0
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            publicationInfoCreator.create(
                contributorId = command.contributorId,
                subjectId = state.paperId!!,
                publicationInfo = command.publicationInfo!!
            )
        }
    }
}
