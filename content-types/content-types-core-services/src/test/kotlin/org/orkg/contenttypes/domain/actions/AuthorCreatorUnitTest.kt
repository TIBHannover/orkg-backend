package org.orkg.contenttypes.domain.actions

import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import java.net.URI
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.contenttypes.domain.Author
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicates
import org.orkg.graph.input.CreateListUseCase
import org.orkg.graph.input.CreateLiteralUseCase.CreateCommand
import org.orkg.graph.input.CreateResourceUseCase
import org.orkg.graph.input.ListUseCases
import org.orkg.graph.input.LiteralUseCases
import org.orkg.graph.input.ResourceUseCases
import org.orkg.graph.input.StatementUseCases
import org.orkg.graph.testing.fixtures.createLiteral

class AuthorCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val authorCreator =
        object : AuthorCreator(resourceService, statementService, literalService, listService) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, statementService, literalService, listService)
    }

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
                CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        } returns orcidLiteral.id
        every {
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasORCID,
                `object` = orcidLiteral.id
            )
        } just runs
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
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        } just runs

        authorCreator.create(contributorId, listOf(author), subjectId)

        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasORCID,
                `object` = orcidLiteral.id
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
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
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
                CreateCommand(
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
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        } just runs

        authorCreator.create(contributorId, listOf(author), subjectId)

        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
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
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
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
            homepage = URI.create("https://orkg.org")
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

        every { resourceService.create(resourceCreateCommand) } returns authorId
        every {
            literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        } returns orcidLiteralId
        every {
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasORCID,
                `object` = orcidLiteralId
            )
        } just runs
        every {
            literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = author.homepage.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        } returns homepageLiteralId
        every {
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasWebsite,
                `object` = homepageLiteralId
            )
        } just runs
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
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        } just runs

        authorCreator.create(contributorId, listOf(author), subjectId)

        verify(exactly = 1) { resourceService.create(resourceCreateCommand) }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = orcid
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasORCID,
                `object` = orcidLiteralId
            )
        }
        verify(exactly = 1) {
            literalService.create(
                CreateCommand(
                    contributorId = contributorId,
                    label = author.homepage.toString(),
                    datatype = Literals.XSD.URI.prefixedUri
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = contributorId,
                subject = authorId,
                predicate = Predicates.hasWebsite,
                `object` = homepageLiteralId
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
                userId = contributorId,
                subject = subjectId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        }
    }
}
