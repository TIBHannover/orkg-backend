package org.orkg.contenttypes.domain.actions.literaturelists

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
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateLiteratureListCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

class LiteratureListExistenceValidatorUnitTest {
    private val literatureListService: LiteratureListService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val literatureListExistenceValidator = LiteratureListExistenceValidator(literatureListService, resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literatureListService, resourceRepository)
    }

    @Test
    fun `Given a literature list update command, when checking for literature list existence, it returns success`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyUpdateLiteratureListCommand().copy(literatureListId = literatureList.id)
        val state = UpdateLiteratureListState()
        val root = createResource(id = literatureList.id, classes = setOf(Classes.literatureList))
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(LiteratureList.Companion) {
            every { resourceRepository.findById(literatureList.id) } returns Optional.of(root)
            every { literatureListService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { LiteratureList.from(root, root.id, statements) } returns literatureList

            literatureListExistenceValidator(command, state).asClue {
                it.literatureList shouldBe literatureList
                it.statements shouldBe statements
                it.authors shouldBe emptyList()
            }

            verify(exactly = 1) { resourceRepository.findById(literatureList.id) }
            verify(exactly = 1) { literatureListService.findSubgraph(root) }
            verify(exactly = 1) { LiteratureList.from(root, root.id, statements) }
        }
    }

    @Test
    fun `Given a literature list update command, when checking for literature list existence and literature list is not found, it throws an exception`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyUpdateLiteratureListCommand().copy(literatureListId = literatureList.id)
        val state = UpdateLiteratureListState()

        every { resourceRepository.findById(literatureList.id) } returns Optional.empty()

        shouldThrow<LiteratureListNotFound> { literatureListExistenceValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(literatureList.id) }
    }
}
