package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import org.junit.jupiter.api.Test
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.actions.PublishPaperState
import org.orkg.contenttypes.domain.actions.SingleStatementPropertyCreator
import org.orkg.contenttypes.domain.identifiers.DOI
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.publishPaperCommand
import org.orkg.contenttypes.output.DoiService
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates

internal class PaperVersionDoiPublisherUnitTest : MockkBaseTest {
    private val singleStatementPropertyCreator: SingleStatementPropertyCreator = mockk()
    private val doiService: DoiService = mockk()

    private val paperVersionArchiver = PaperVersionDoiPublisher(
        singleStatementPropertyCreator = singleStatementPropertyCreator,
        doiService = doiService,
        paperPublishBaseUri = "https://orkg.org/paper/"
    )

    @Test
    fun `Given a paper publish command, it registers a new doi creates a hasDOI statement`() {
        val paper = createPaper()
        val command = publishPaperCommand().copy(id = paper.id)
        val paperVersionId = ThingId("R321")
        val state = PublishPaperState(
            paper = paper,
            paperVersionId = paperVersionId
        )
        val doi = "10.1000/182"

        every { doiService.register(any()) } returns DOI.of(doi)
        every { singleStatementPropertyCreator.create(any(), any(), any(), any()) } just runs

        paperVersionArchiver(command, state).asClue {
            it.paper shouldBe paper
            it.paperVersionId shouldBe paperVersionId
        }

        verify(exactly = 1) {
            doiService.register(withArg {
                it.suffix shouldBe paperVersionId.value
                it.title shouldBe paper.title
                it.subject shouldBe command.subject
                it.description shouldBe command.description
                it.url shouldBe URI.create("https://orkg.org/paper/${paperVersionId.value}")
                it.creators shouldBe paper.authors
                it.resourceType shouldBe Classes.paper.value
                it.resourceTypeGeneral shouldBe "Dataset"
                it.relatedIdentifiers shouldBe emptyList()
            })
        }
        verify(exactly = 1) {
            singleStatementPropertyCreator.create(
                contributorId = command.contributorId,
                subjectId = paperVersionId,
                predicateId = Predicates.hasDOI,
                label = doi
            )
        }
    }
}
