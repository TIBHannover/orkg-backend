package org.orkg.contenttypes.domain.actions

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.StatementId
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.CreateStatementUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.input.UnsafeResourceUseCases
import org.orkg.graph.testing.fixtures.createLiteral

internal class AuthorCreatorUnitTest : MockkBaseTest {
    private val unsafeResourceUseCases: UnsafeResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val authorCreator =
        object : AuthorCreator(unsafeResourceUseCases, statementService, literalService, listService) {}

    @Test
    fun `Given a list of authors, when linking an existing author to the subject resource, it returns success`() {
        val subjectId = ThingId("R123")
        val authorId = ThingId("R456")
        val orcid = "0000-1111-2222-3333"
        val author = Author(
            id = authorId,
            name = "Author",
            identifiers = mapOf(
                "orcid" to listOf(orcid)
            )
        )
        val authorListId = ThingId("R1456")
        val contributorId = ContributorId(UUID.randomUUID())
        val orcidLiteral = createLiteral(
            id = ThingId(UUID.randomUUID().toString()),
            label = author.name
        )

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        } returns orcidLiteral.id
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = Predicates.hasORCID,
                    objectId = orcidLiteral.id
                )
            )
        } returns StatementId("S1")
        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = contributorId
                )
            )
        } returns authorListId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasAuthors,
                    objectId = authorListId
                )
            )
        } returns StatementId("S2")

        authorCreator.create(contributorId, listOf(author), subjectId)

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = Predicates.hasORCID,
                    objectId = orcidLiteral.id
                )
            )
        }
        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasAuthors,
                    objectId = authorListId
                )
            )
        }
    }

    @Test
    fun `Given a list of authors, it crates a new author literal and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val author = Author(
            name = "Author"
        )
        val authorId = ThingId("R456")
        val literal = createLiteral(id = authorId, label = author.name)
        val authorListId = ThingId("R1456")
        val contributorId = ContributorId(UUID.randomUUID())

        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = author.name
                )
            )
        } returns literal.id
        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = contributorId
                )
            )
        } returns authorListId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasAuthors,
                    objectId = authorListId
                )
            )
        } returns StatementId("S1")

        authorCreator.create(contributorId, listOf(author), subjectId)

        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = author.name
                )
            )
        }
        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasAuthors,
                    objectId = authorListId
                )
            )
        }
    }

    @Test
    fun `Given a list of authors, it crates a new author resource and links it to the subject resource`() {
        val subjectId = ThingId("R123")
        val orcid = "0000-1111-2222-3333"
        val author = Author(
            name = "Author",
            identifiers = mapOf(
                "orcid" to listOf(orcid)
            ),
            homepage = ParsedIRI("https://orkg.org")
        )
        val authorId = ThingId("R456")
        val contributorId = ContributorId(UUID.randomUUID())
        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = author.name,
            classes = setOf(Classes.author),
            contributorId = contributorId
        )
        val orcidLiteralId = ThingId("L65412")
        val homepageLiteralId = ThingId("L13254")
        val authorListId = ThingId("R1456")

        every { unsafeResourceUseCases.create(resourceCreateCommand) } returns authorId
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        } returns orcidLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = Predicates.hasORCID,
                    objectId = orcidLiteralId
                )
            )
        } returns StatementId("S1")
        every {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = author.homepage.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns homepageLiteralId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = Predicates.hasWebsite,
                    objectId = homepageLiteralId
                )
            )
        } returns StatementId("S2")
        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = contributorId
                )
            )
        } returns authorListId
        every {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasAuthors,
                    objectId = authorListId
                )
            )
        } returns StatementId("S3")

        authorCreator.create(contributorId, listOf(author), subjectId)

        verify(exactly = 1) { unsafeResourceUseCases.create(resourceCreateCommand) }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = Predicates.hasORCID,
                    objectId = orcidLiteralId
                )
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateLiteralUseCase.CreateCommand(
                    contributorId = contributorId,
                    label = author.homepage.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = authorId,
                    predicateId = Predicates.hasWebsite,
                    objectId = homepageLiteralId
                )
            )
        }
        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                CreateStatementUseCase.CreateCommand(
                    contributorId = contributorId,
                    subjectId = subjectId,
                    predicateId = Predicates.hasAuthors,
                    objectId = authorListId
                )
            )
        }
    }
}
