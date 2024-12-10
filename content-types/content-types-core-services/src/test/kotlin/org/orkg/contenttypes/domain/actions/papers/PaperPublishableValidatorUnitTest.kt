package org.orkg.contenttypes.domain.actions.papers

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.Paper
import org.orkg.contenttypes.domain.PaperNotFound
import org.orkg.contenttypes.domain.PaperService
import org.orkg.contenttypes.domain.actions.PublishPaperState
import org.orkg.contenttypes.domain.testing.fixtures.createPaper
import org.orkg.contenttypes.input.testing.fixtures.createPaperPublishCommand
import org.orkg.contenttypes.input.testing.fixtures.dummyPublishPaperCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

internal class PaperPublishableValidatorUnitTest {
    private val paperService: PaperService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val paperPublishableValidator = PaperPublishableValidator(paperService, resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(paperService, resourceRepository)
    }

    @Test
    fun `Given a paper publish command, when checking for paper existence, it returns success`() {
        val paper = createPaper()
        val command = createPaperPublishCommand().copy(id = paper.id)
        val state = PublishPaperState()
        val root = createResource(
            id = paper.id,
            label = paper.title,
            classes = setOf(Classes.paper)
        )
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(Paper.Companion) {
            every { resourceRepository.findById(paper.id) } returns Optional.of(root)
            every { paperService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { Paper.from(root, statements) } returns paper

            paperPublishableValidator(command, state).asClue {
                it.paper shouldBe paper
                it.statements shouldBe statements
                it.paperVersionId shouldBe null
            }

            verify(exactly = 1) { resourceRepository.findById(paper.id) }
            verify(exactly = 1) { paperService.findSubgraph(root) }
            verify(exactly = 1) { Paper.from(root, statements) }
        }
    }

    @Test
    fun `Given a paper publish command, when checking for paper existence and paper is not found, it throws an exception`() {
        val paper = createPaper()
        val command = dummyPublishPaperCommand().copy(id = paper.id)
        val state = PublishPaperState()

        every { resourceRepository.findById(paper.id) } returns Optional.empty()

        shouldThrow<PaperNotFound> { paperPublishableValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(paper.id) }
    }
}
