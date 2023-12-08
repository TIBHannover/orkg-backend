package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.PageRequests
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.testing.pageOf

class AuthorUpdaterUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()
    private val authorCreator: AuthorCreator = mockk()

    private val authorUpdater =
        object : AuthorUpdater(resourceService, statementService, literalService, listService, authorCreator) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, statementService, literalService, listService, authorCreator)
    }

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

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = subjectId,
                predicateId = Predicates.hasAuthors,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf()
        every { authorCreator.create(contributorId, authors, subjectId) } just runs

        authorUpdater.update(contributorId, authors, subjectId)

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = subjectId,
                predicateId = Predicates.hasAuthors,
                pagination = PageRequests.SINGLE
            )
        }
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

        every {
            statementService.findAllBySubjectAndPredicate(
                subjectId = subjectId,
                predicateId = Predicates.hasAuthors,
                pagination = PageRequests.SINGLE
            )
        } returns pageOf(
            createStatement(
                subject = createResource(subjectId),
                predicate = createPredicate(Predicates.hasAuthors),
                `object` = createResource(authorListId, classes = setOf(Classes.list))
            )
        )
        every { listService.delete(authorListId) } just runs
        every { authorCreator.create(contributorId, authors, subjectId) } just runs

        authorUpdater.update(contributorId, authors, subjectId)

        verify(exactly = 1) {
            statementService.findAllBySubjectAndPredicate(
                subjectId = subjectId,
                predicateId = Predicates.hasAuthors,
                pagination = PageRequests.SINGLE
            )
        }
        verify(exactly = 1) { listService.delete(authorListId) }
        verify(exactly = 1) { authorCreator.create(contributorId, authors, subjectId) }
    }
}
