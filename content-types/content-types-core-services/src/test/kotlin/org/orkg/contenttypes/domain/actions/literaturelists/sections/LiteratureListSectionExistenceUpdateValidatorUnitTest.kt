package org.orkg.contenttypes.domain.actions.literaturelists.sections

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
import org.orkg.contenttypes.domain.actions.UpdateLiteratureListSectionState
import org.orkg.contenttypes.domain.testing.fixtures.createDummyLiteratureList
import org.orkg.contenttypes.input.testing.fixtures.dummyUpdateTextSectionCommand
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement

class LiteratureListSectionExistenceUpdateValidatorUnitTest {
    private val literatureListService: LiteratureListService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val literatureListSectionExistenceUpdateValidator =
        LiteratureListSectionExistenceUpdateValidator(literatureListService, resourceRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(literatureListService, resourceRepository)
    }

    @Test
    fun `Given a literature list section update command, when checking for literature list existence, it returns success`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyUpdateTextSectionCommand().copy(literatureListId = literatureList.id)
        val state = UpdateLiteratureListSectionState()
        val root = createResource(id = literatureList.id, classes = setOf(Classes.literatureList))
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(LiteratureList.Companion) {
            every { resourceRepository.findById(literatureList.id) } returns Optional.of(root)
            every { literatureListService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { LiteratureList.from(root, root.id, statements) } returns literatureList

            literatureListSectionExistenceUpdateValidator(command, state).asClue {
                it.literatureList shouldBe literatureList
                it.statements shouldBe statements
            }

            verify(exactly = 1) { resourceRepository.findById(literatureList.id) }
            verify(exactly = 1) { literatureListService.findSubgraph(root) }
            verify(exactly = 1) { LiteratureList.from(root, root.id, statements) }
        }
    }

    @Test
    fun `Given a literature list section update command, when literature list is not found, it throws an exception`() {
        val literatureList = createDummyLiteratureList()
        val command = dummyUpdateTextSectionCommand().copy(literatureListId = literatureList.id)
        val state = UpdateLiteratureListSectionState()

        every { resourceRepository.findById(literatureList.id) } returns Optional.empty()

        shouldThrow<LiteratureListNotFound> { literatureListSectionExistenceUpdateValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(literatureList.id) }
    }
}
