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

    private val authorValidator = object : AuthorValidator(resourceRepository, statementRepository) {}

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, statementRepository)
    }

    @Test
    fun `Given a list of authors, when validating, it returns success`() {
        val authors = dummyCreatePaperCommand().authors

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

        val result = authorValidator.validate(authors)

        result shouldBe listOf(
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
    fun `Given a list of authors, when author does not exist, it throws an exception`() {
        val authors = dummyCreatePaperCommand().authors

        every { resourceRepository.findById(any()) } returns Optional.empty()

        assertThrows<AuthorNotFound> { authorValidator.validate(authors) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }

    @Test
    fun `Given a list of authors, when author is not an author resource, it throws an exception`() {
        val authors = dummyCreatePaperCommand().authors

        every { resourceRepository.findById(any()) } returns Optional.of(createResource())

        assertThrows<AuthorNotFound> { authorValidator.validate(authors) }

        verify(exactly = 1) { resourceRepository.findById(any()) }
    }

    @Test
    fun `Given a list of authors, when author is ambiguous because id and identifier match different resources, it throws an exception`() {
        val authors = listOf(
            Author(
                id = ThingId("R123"),
                name = "Author with orcid",
                identifiers = mapOf("orcid" to "0000-1111-2222-3333")
            )
        )

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

        assertThrows<AmbiguousAuthor> { authorValidator.validate(authors) }

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
    fun `Given a list of authors, when author is ambiguous because identifiers match different resources, it throws an exception`() {
        val authors = listOf(
            Author(
                id = ThingId("R123"),
                name = "Author with orcid",
                identifiers = mapOf(
                    "orcid" to "0000-1111-2222-3333",
                    "research_gate" to "1111-2222-3333-4444"
                )
            )
        )

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

        assertThrows<AmbiguousAuthor> { authorValidator.validate(authors) }

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
    fun `Given a list of authors, when author identifier is structurally invalid, it throws an exception`() {
        val authors = listOf(
            Author(
                name = "Invalid Author",
                identifiers = mapOf(
                    "orcid" to "invalid"
                )
            )
        )

        assertThrows<InvalidIdentifier> { authorValidator.validate(authors) }.property shouldBe "orcid"
    }
}
