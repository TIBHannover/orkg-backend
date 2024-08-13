package org.orkg.contenttypes.domain.actions.papers.snapshot

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperService
import org.orkg.contenttypes.domain.actions.SnapshotPaperState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyPaper
import org.orkg.contenttypes.input.testing.fixtures.createPaperPublishCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

class PaperSnapshotPaperLoaderUnitTest {
    private val paperService: PaperService = mockk()

    private val paperSnapshotPaperLoader = PaperSnapshotPaperLoader(paperService)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(paperService)
    }

    @Test
    fun `Given a paper update command, when checking for paper existence, it returns success`() {
        val paper = createDummyPaper()
        val command = createPaperPublishCommand()
        val resource = createResource(
            id = paper.id,
            label = paper.title,
            classes = setOf(Classes.paper)
        )
        val state = SnapshotPaperState(resource)
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(Paper.Companion) {
            every { paperService.findSubgraph(resource) } returns ContentTypeSubgraph(resource.id, statements)
            every { Paper.from(resource, statements) } returns paper

            paperSnapshotPaperLoader(command, state).asClue {
                it.resource shouldBe resource
                it.paper shouldBe paper
                it.statements shouldBe statements
                it.paperVersionId shouldBe null
            }

            verify(exactly = 1) { paperService.findSubgraph(resource) }
            verify(exactly = 1) { Paper.from(resource, statements) }
        }
    }
}
