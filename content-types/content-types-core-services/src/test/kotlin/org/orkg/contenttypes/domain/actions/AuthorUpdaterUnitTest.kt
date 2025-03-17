package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Predicates
import org.orkg.graph.output.ListRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import java.util.UUID

internal class AuthorUpdaterUnitTest : MockkBaseTest {
    private val authorCreator: AbstractAuthorListCreator = mockk()
    private val listRepository: ListRepository = mockk()

    private val authorUpdater = AbstractAuthorListUpdater(listRepository, authorCreator)

    @Test
    fun `Given a subject resource without author list, it creates a new author list`() {
        val subjectId = ThingId("R123")
        val authorId = ThingId("R456")
        val authors = listOf(
            Author(
                id = authorId,
                name = "Author"
            )
        )
        val contributorId = ContributorId(UUID.randomUUID())
        val statements = emptyMap<ThingId, List<GeneralStatement>>()

        every { authorCreator.create(contributorId, authors, subjectId) } just runs

        authorUpdater.update(statements, contributorId, authors, subjectId)

        verify(exactly = 1) { authorCreator.create(contributorId, authors, subjectId) }
    }

    @Test
    fun `Given a subject resource with an existing author list, it replaces the author list`() {
        val subjectId = ThingId("R123")
        val authorId = ThingId("R456")
        val authors = listOf(
            Author(
                id = authorId,
                name = "Author"
            )
        )
        val authorListId = ThingId("R1456")
        val contributorId = ContributorId(UUID.randomUUID())
        val statements = mapOf(
            subjectId to listOf(
                createStatement(
                    subject = createResource(subjectId),
                    predicate = createPredicate(Predicates.hasAuthors),
                    `object` = createResource(authorListId, classes = setOf(Classes.list))
                )
            )
        )

        every { listRepository.deleteById(authorListId) } just runs
        every { authorCreator.create(contributorId, authors, subjectId) } just runs

        authorUpdater.update(statements, contributorId, authors, subjectId)

        verify(exactly = 1) { listRepository.deleteById(authorListId) }
        verify(exactly = 1) { authorCreator.create(contributorId, authors, subjectId) }
    }
}
