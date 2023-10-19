package eu.tib.orkg.prototype.contenttypes.services.actions

import eu.tib.orkg.prototype.contenttypes.application.AmbiguousAuthor
import eu.tib.orkg.prototype.contenttypes.application.AuthorNotFound
import eu.tib.orkg.prototype.contenttypes.domain.model.Author
import eu.tib.orkg.prototype.contenttypes.testing.fixtures.dummyCreatePaperCommand
import eu.tib.orkg.prototype.identifiers.application.InvalidIdentifier
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.statements.api.Classes
import eu.tib.orkg.prototype.statements.api.Predicates
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.statements.testing.fixtures.createPredicate
import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.statements.testing.fixtures.createStatement
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.net.URI
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page

class AuthorValidatorUnitTest {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val authorValidator = AuthorValidator(resourceRepository, statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, statementRepository)
    }

    @Test
    fun `Given a paper create command, when validating its authors, it returns success`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        val author1 = createResource(id = ThingId("R123"), classes = setOf(Classes.author))
        val author2 = createResource(id = ThingId("R456"), classes = setOf(Classes.author))

        every { resourceRepository.findById(author1.id) } returns Optional.of(author1)
        every { resourceRepository.findById(author2.id) } returns Optional.of(author2)
        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "0000-1111-2222-3333",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        } returns Page.empty()
        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "1111-2222-3333-4444",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                subject = author2,
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = "1111-2222-3333-4444")
            )
        )

        val result = authorValidator(command, state)

        result.asClue {
            it.tempIds.size shouldBe 0
            it.validatedIds.size shouldBe 0
            it.bakedStatements.size shouldBe 0
            it.authors shouldBe listOf(
                Author(
                    id = ThingId("R123"),
                    name = "Author with id"
                ),
                Author(
                    name = "Author with orcid",
                    identifiers = mapOf("orcid" to "0000-1111-2222-3333")
                ),
                Author(
                    id = ThingId("R456"),
                    name = "Author with id and orcid"
                ),
                Author(
                    name = "Author with homepage",
                    homepage = URI.create("http://example.org/author")
                ),
                Author(
                    name = "Author that just has a name"
                )
            )
            it.paperId shouldBe null
        }

        verify(exactly = 1) { resourceRepository.findById(author1.id) }
        verify(exactly = 1) { resourceRepository.findById(author2.id) }
        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "0000-1111-2222-3333",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "1111-2222-3333-4444",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a paper create command, when author does not exist, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        every { resourceRepository.findById(any()) } returns Optional.empty()

        assertThrows<AuthorNotFound> { authorValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }

    @Test
    fun `Given a paper create command, when author is not a author resource, it throws an exception`() {
        val command = dummyCreatePaperCommand()
        val state = PaperState()

        every { resourceRepository.findById(any()) } returns Optional.of(createResource())

        assertThrows<AuthorNotFound> { authorValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }

    @Test
    fun `Given a paper create command, when author is ambiguous because id and identifier match different resources, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(
            authors = listOf(
                Author(
                    id = ThingId("R123"),
                    name = "Author with orcid",
                    identifiers = mapOf("orcid" to "0000-1111-2222-3333")
                )
            )
        )
        val state = PaperState()

        val author1 = createResource(id = ThingId("R123"), classes = setOf(Classes.author))
        val author2 = createResource(id = ThingId("R456"), classes = setOf(Classes.author))

        every { resourceRepository.findById(author1.id) } returns Optional.of(author1)
        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "0000-1111-2222-3333",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                subject = author2,
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = "0000-1111-2222-3333")
            )
        )

        assertThrows<AmbiguousAuthor> { authorValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(author1.id) }
        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "0000-1111-2222-3333",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a paper create command, when author is ambiguous because identifiers match different resources, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(
            authors = listOf(
                Author(
                    id = ThingId("R123"),
                    name = "Author with orcid",
                    identifiers = mapOf(
                        "orcid" to "0000-1111-2222-3333",
                        "research_gate" to "1111-2222-3333-4444"
                    )
                )
            )
        )
        val state = PaperState()

        val author1 = createResource(id = ThingId("R123"), classes = setOf(Classes.author))
        val author2 = createResource(id = ThingId("R456"), classes = setOf(Classes.author))

        every { resourceRepository.findById(author1.id) } returns Optional.of(author1)
        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "0000-1111-2222-3333",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                subject = author1,
                predicate = createPredicate(Predicates.hasORCID),
                `object` = createLiteral(label = "0000-1111-2222-3333")
            )
        )
        every {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasResearchGateId,
                literal = "1111-2222-3333-4444",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        } returns pageOf(
            createStatement(
                subject = author2,
                predicate = createPredicate(Predicates.hasResearchGateId),
                `object` = createLiteral(label = "1111-2222-3333-4444")
            )
        )

        assertThrows<AmbiguousAuthor> { authorValidator(command, state) }

        verify(exactly = 1) { resourceRepository.findById(author1.id) }
        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasORCID,
                literal = "0000-1111-2222-3333",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        }
        verify(exactly = 1) {
            statementRepository.findAllByPredicateIdAndLabelAndSubjectClass(
                predicateId = Predicates.hasResearchGateId,
                literal = "1111-2222-3333-4444",
                subjectClass = Classes.author,
                pageable = PageRequests.ALL
            )
        }
    }

    @Test
    fun `Given a paper create command, when author identifier is structurally invalid, it throws an exception`() {
        val command = dummyCreatePaperCommand().copy(
            authors = listOf(
                Author(
                    name = "Invalid Author",
                    identifiers = mapOf(
                        "orcid" to "invalid"
                    )
                )
            )
        )
        val state = PaperState()

        assertThrows<InvalidIdentifier> { authorValidator(command, state) }.property shouldBe "orcid"
    }
}
