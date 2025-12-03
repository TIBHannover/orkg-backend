package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.PaperService
import org.orkg.contenttypes.domain.actions.DeletePaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.deletePaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.Optional

internal class PaperExistenceDeleteValidatorUnitTest : MockkBaseTest {
    private val paperService: PaperService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val paperExistenceDeleteValidator = PaperExistenceDeleteValidator(paperService, resourceRepository)

    @Test
    fun `Given a paper delete command, when checking for paper existence, it returns success`() {
        val paper = createPaper()
        val command = deletePaperCommand().copy(paperId = paper.id)
        val state = DeletePaperState()
        val root = createResource(
            id = paper.id,
            label = paper.title,
            classes = setOf(Classes.paper)
        )
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        every { resourceRepository.findById(paper.id) } returns Optional.of(root)
        every { paperService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)

        paperExistenceDeleteValidator(command, state).asClue {
            it.paper shouldBe root
            it.statements shouldBe statements
        }

        verify(exactly = 1) { resourceRepository.findById(paper.id) }
        verify(exactly = 1) { paperService.findSubgraph(root) }
    }

    @Test
    fun `Given a paper delete command, when checking for paper existence and paper is not found, it does not throw an exception`() {
        val paper = createPaper()
        val command = deletePaperCommand().copy(paperId = paper.id)
        val state = DeletePaperState()

        every { resourceRepository.findById(paper.id) } returns Optional.empty()

        paperExistenceDeleteValidator(command, state) shouldBe state

        verify(exactly = 1) { resourceRepository.findById(paper.id) }
    }
}
