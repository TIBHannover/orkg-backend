package eu.tib.orkg.prototype.contenttypes.services

import eu.tib.orkg.prototype.contenttypes.application.PaperNotFound
import eu.tib.orkg.prototype.contenttypes.services.PaperService
import eu.tib.orkg.prototype.createResource
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.domain.model.Visibility
import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl

class PaperServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = PaperService(resourceRepository, statementRepository)

    @Test
    fun `Given a paper exists, when fetching it by id, then it is returned`() {
        val expected = createResource()
        every { resourceRepository.findPaperById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllBySubject(expected.id, PageRequests.ALL) } returns Page.empty(PageRequests.ALL)

        val actual = service.findById(expected.id)

        actual.isPresent shouldBe true
        actual.get().asClue { paper ->
            paper.id shouldBe expected.id
            paper.title shouldBe expected.label
            paper.researchFields shouldNotBe null
            paper.researchFields shouldBe emptyList()
            paper.identifiers shouldNotBe null
            paper.identifiers shouldBe emptyMap()
            paper.publicationInfo shouldNotBe null
            paper.publicationInfo.asClue { publicationInfo ->
                publicationInfo.publishedMonth shouldBe null
                publicationInfo.publishedYear shouldBe null
                publicationInfo.publishedIn shouldBe null
                publicationInfo.url shouldBe null
            }
            paper.authors shouldNotBe null
            paper.authors shouldBe emptyList()
            paper.contributions shouldNotBe null
            paper.contributions shouldBe emptyList()
            paper.observatories shouldBe setOf(expected.observatoryId)
            paper.organizations shouldBe setOf(expected.organizationId)
            paper.extractionMethod shouldBe expected.extractionMethod
            paper.createdAt shouldBe expected.createdAt
            paper.createdBy shouldBe expected.createdBy
            paper.verified shouldBe false
            paper.visibility shouldBe Visibility.DEFAULT
        }

        verify(exactly = 1) { resourceRepository.findPaperById(expected.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(expected.id, any()) }
    }

    @Test
    fun `Given a paper, when fetching its contributors, then the list of contributors returned`() {
        val expected = createResource()
        every { resourceRepository.findPaperById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllContributorsByResourceId(expected.id, PageRequests.ALL) } returns PageImpl(
            listOf(expected.createdBy),
            PageRequests.ALL,
            1
        )

        service.findAllContributorsByPaperId(expected.id, PageRequests.ALL)

        verify(exactly = 1) { resourceRepository.findPaperById(expected.id) }
        verify(exactly = 1) { statementRepository.findAllContributorsByResourceId(expected.id, any()) }
    }

    @Test
    fun `Given a paper does not exist, when fetching its contributors, then an exception is thrown`() {
        val id = ThingId("Missing")
        every { resourceRepository.findPaperById(id) } returns Optional.empty()

        assertThrows<PaperNotFound> {
            service.findAllContributorsByPaperId(id, PageRequests.ALL)
        }

        verify(exactly = 1) { resourceRepository.findPaperById(id) }
        verify(exactly = 0) { statementRepository.findAllContributorsByResourceId(id, any()) }
    }
}
