package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.CreateListUseCase
import eu.tib.orkg.prototype.statements.api.CreateResourceUseCase
import eu.tib.orkg.prototype.statements.api.ListUseCases
import eu.tib.orkg.prototype.statements.api.LiteralUseCases
import eu.tib.orkg.prototype.statements.api.Literals
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.api.ResourceUseCases
import eu.tib.orkg.prototype.statements.api.StatementUseCases
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
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

class PaperAuthorCreatorUnitTest {
    private val resourceService: ResourceUseCases = mockk()
    private val statementService: StatementUseCases = mockk()
    private val literalService: LiteralUseCases = mockk()
    private val listService: ListUseCases = mockk()

    private val paperAuthorCreator = PaperAuthorCreator(
        resourceService = resourceService,
        statementService = statementService,
        literalService = literalService,
        listService = listService
    )

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceService, statementService, literalService, listService)
    }

    @Test
    fun `Given a paper create command, when linking an existing author to the paper, it returns success`() {
        val paperId = ThingId("R123")
        val command = dummyCreatePaperCommand()
        val authorId = ThingId("R456")
        val author = Author(
            id = authorId,
            name = "Author"
        )
        val state = PaperState(
            authors = listOf(author),
            paperId = paperId
        )
        val authorListId = ThingId("R1456")

        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = command.contributorId
                )
            )
        } returns authorListId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        } just runs

        val result = paperAuthorCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors shouldBe state.authors
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        }
    }

    @Test
    fun `Given a paper create command, it crates a new author literal and links it to the paper`() {
        val paperId = ThingId("R123")
        val command = dummyCreatePaperCommand()
        val author = Author(
            name = "Author"
        )
        val authorId = ThingId("R456")
        val state = PaperState(
            authors = listOf(author),
            paperId = paperId
        )
        val literal = createLiteral(id = authorId, label = author.name)
        val authorListId = ThingId("R1456")

        every {
            literalService.create(
                userId = command.contributorId,
                label = author.name,
                datatype = Literals.XSD.STRING.prefixedUri
            )
        } returns literal
        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = command.contributorId
                )
            )
        } returns authorListId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        } just runs

        val result = paperAuthorCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors shouldBe state.authors
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = author.name,
                datatype = Literals.XSD.STRING.prefixedUri
            )
        }
        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        }
    }

    @Test
    fun `Given a paper create command, it crates a new author resource and links it to the paper`() {
        val paperId = ThingId("R123")
        val command = dummyCreatePaperCommand()
        val orcid = "0000-1111-2222-3333"
        val author = Author(
            name = "Author",
            identifiers = mapOf(
                "orcid" to orcid
            ),
            homepage = URI.create("https://orkg.org")
        )
        val authorId = ThingId("R456")
        val state = PaperState(
            authors = listOf(author),
            paperId = paperId
        )
        val resourceCreateCommand = CreateResourceUseCase.CreateCommand(
            label = author.name,
            classes = setOf(Classes.author),
            contributorId = command.contributorId
        )
        val orcidLiteral = createLiteral(
            id = ThingId(UUID.randomUUID().toString()),
            label = author.name
        )
        val homepageLiteral = createLiteral(
            id = ThingId(UUID.randomUUID().toString()),
            label = author.homepage.toString()
        )
        val authorListId = ThingId("R1456")

        every { resourceService.create(resourceCreateCommand) } returns authorId
        every {
            literalService.create(
                userId = command.contributorId,
                label = orcid,
                datatype = Literals.XSD.STRING.prefixedUri
            )
        } returns orcidLiteral
        every {
            statementService.add(
                userId = command.contributorId,
                subject = authorId,
                predicate = Predicates.hasORCID,
                `object` = orcidLiteral.id
            )
        } just runs
        every {
            literalService.create(
                userId = command.contributorId,
                label = author.homepage.toString(),
                datatype = Literals.XSD.URI.prefixedUri
            )
        } returns homepageLiteral
        every {
            statementService.add(
                userId = command.contributorId,
                subject = authorId,
                predicate = Predicates.hasWebsite,
                `object` = homepageLiteral.id
            )
        } just runs
        every {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = command.contributorId
                )
            )
        } returns authorListId
        every {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        } just runs

        val result = paperAuthorCreator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors shouldBe state.authors
            it.paperId shouldBe state.paperId
        }

        verify(exactly = 1) { resourceService.create(resourceCreateCommand) }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = orcid,
                datatype = Literals.XSD.STRING.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = authorId,
                predicate = Predicates.hasORCID,
                `object` = orcidLiteral.id
            )
        }
        verify(exactly = 1) {
            literalService.create(
                userId = command.contributorId,
                label = author.homepage.toString(),
                datatype = Literals.XSD.URI.prefixedUri
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = authorId,
                predicate = Predicates.hasWebsite,
                `object` = homepageLiteral.id
            )
        }
        verify(exactly = 1) {
            listService.create(
                CreateListUseCase.CreateCommand(
                    label = "authors list",
                    elements = listOf(authorId),
                    contributorId = command.contributorId
                )
            )
        }
        verify(exactly = 1) {
            statementService.add(
                userId = command.contributorId,
                subject = paperId,
                predicate = Predicates.hasAuthors,
                `object` = authorListId
            )
        }
    }
}
