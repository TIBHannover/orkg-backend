package org.orkg.contenttypes.domain.actions.literaturelists

import io.kotest.assertions.asClue
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.ContentTypeSubgraph
import org.orkg.contenttypes.domain.LiteratureList
import org.orkg.contenttypes.domain.LiteratureListNotFound
import org.orkg.contenttypes.domain.LiteratureListNotModifiable
import org.orkg.contenttypes.domain.LiteratureListService
import org.orkg.contenttypes.domain.testing.fixtures.createLiteratureList
import org.orkg.graph.domain.Classes
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.Optional

internal class AbstractLiteratureListExistenceValidatorUnitTest : MockkBaseTest {
    private val literatureListService: LiteratureListService = mockk()
    private val resourceRepository: ResourceRepository = mockk()

    private val abstractLiteratureListExistenceValidator =
        AbstractLiteratureListExistenceValidator(literatureListService, resourceRepository)

    @Test
    fun `Given a literature list id, when checking for literature list existence, it returns success`() {
        val literatureList = createLiteratureList()
        val root = createResource(id = literatureList.id, classes = setOf(Classes.literatureList))
        val statements = listOf(createStatement()).groupBy { it.subject.id }

        mockkObject(LiteratureList.Companion) {
            every { resourceRepository.findById(literatureList.id) } returns Optional.of(root)
            every { literatureListService.findSubgraph(root) } returns ContentTypeSubgraph(root.id, statements)
            every { LiteratureList.from(root, root.id, statements) } returns literatureList

            abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(literatureList.id).asClue {
                it.first shouldBe literatureList
                it.second shouldBe statements
            }

            verify(exactly = 1) { resourceRepository.findById(literatureList.id) }
            verify(exactly = 1) { literatureListService.findSubgraph(root) }
            verify(exactly = 1) { LiteratureList.from(root, root.id, statements) }
        }
    }

    @Test
    fun `Given a literature list id, when literature list is published, it throws an exception`() {
        val literatureListId = ThingId("R123")
        val root = createResource(id = literatureListId, classes = setOf(Classes.literatureListPublished))

        every { resourceRepository.findById(literatureListId) } returns Optional.of(root)

        assertThrows<LiteratureListNotModifiable> {
            abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(literatureListId)
        }

        verify(exactly = 1) { resourceRepository.findById(literatureListId) }
    }

    @Test
    fun `Given a literature list id command, when literature list is not found, it throws an exception`() {
        val literatureListId = ThingId("R123")

        every { resourceRepository.findById(literatureListId) } returns Optional.empty()

        shouldThrow<LiteratureListNotFound> {
            abstractLiteratureListExistenceValidator.findUnpublishedLiteratureListById(literatureListId)
        }

        verify(exactly = 1) { resourceRepository.findById(literatureListId) }
    }
}
