package eu.tib.orkg.prototype.contenttypes.services


import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.shared.PageRequests
import eu.tib.orkg.prototype.statements.api.Classes
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
import org.springframework.data.domain.Page

class ContributionServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = ContributionService(resourceRepository, statementRepository)

    @Test
    fun `Given a contribution exists, when fetching it by id, then it is returned`() {
        val expected = createResource(
            classes = setOf(Classes.contribution)
        )
        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllBySubject(expected.id, PageRequests.ALL) } returns Page.empty(PageRequests.ALL)

        val actual = service.findById(expected.id)
        actual.isPresent shouldBe true
        actual.get() shouldNotBe null
        actual.get().asClue { contribution ->
            contribution.id shouldBe expected.id
            contribution.label shouldBe expected.label
            contribution.properties shouldNotBe null
            contribution.properties shouldBe emptyMap()
            contribution.visibility shouldBe Visibility.DEFAULT
        }

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(expected.id, any()) }
    }

    @Test
    fun `Given a resource, when fetching it as a contribution although its not a contribution, then it returns an empty result`() {
        val expected = createResource()
        every { resourceRepository.findById(expected.id) } returns Optional.of(expected)

        service.findById(expected.id).isPresent shouldBe false

        verify(exactly = 1) { resourceRepository.findById(expected.id) }
        verify(exactly = 0) { statementRepository.findAllBySubject(expected.id, any()) }
    }
}
