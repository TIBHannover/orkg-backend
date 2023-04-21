package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.content_types.application.ContributionNotFound
import eu.tib.orkg.prototype.createResource
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

class ContributionServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = ContributionService(resourceRepository, statementRepository)

    @Test
    fun `Given a contribution exists, when fetching it by id, then it is returned`() {
        val expected = createResource().copy(
            classes = setOf(Classes.contribution)
        )
        every { resourceRepository.findByResourceId(expected.id) } returns Optional.of(expected)
        every { statementRepository.findAllBySubject(expected.id, PageRequests.ALL) } returns Page.empty(PageRequests.ALL)

        service.findById(expected.id).asClue { contribution ->
            contribution.id shouldBe expected.id
            contribution.label shouldBe expected.label
            contribution.properties shouldNotBe null
            contribution.properties shouldBe emptyMap()
            contribution.visibility shouldBe Visibility.DEFAULT
        }

        verify(exactly = 1) { resourceRepository.findByResourceId(expected.id) }
        verify(exactly = 1) { statementRepository.findAllBySubject(expected.id, any()) }
    }

    @Test
    fun `Given a contribution does not exist, when fetching it by id, then an exception is thrown`() {
        val id = ThingId("Missing")
        every { resourceRepository.findByResourceId(id) } returns Optional.empty()

        assertThrows<ContributionNotFound> {
            service.findById(id)
        }

        verify(exactly = 1) { resourceRepository.findByResourceId(id) }
        verify(exactly = 0) { statementRepository.findAllBySubject(id, any()) }
    }

    @Test
    fun `Given a resource, when fetching it as a contribution although its not a contribution, then an exception is thrown`() {
        val expected = createResource()
        every { resourceRepository.findByResourceId(expected.id) } returns Optional.of(expected)

        assertThrows<ContributionNotFound> {
            service.findById(expected.id)
        }

        verify(exactly = 1) { resourceRepository.findByResourceId(expected.id) }
        verify(exactly = 0) { statementRepository.findAllBySubject(expected.id, any()) }
    }
}
