package org.orkg.contenttypes.domain

import io.kotest.assertions.asClue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orkg.common.PageRequests
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Visibility
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createResource
import org.springframework.data.domain.Page

class ContributionServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = ContributionService(resourceRepository, statementRepository)

    @BeforeEach
    fun resetState() {
        clearAllMocks()
    }

    @AfterEach
    fun verifyMocks() {
        confirmVerified(resourceRepository, statementRepository)
    }

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
            contribution.createdAt shouldBe expected.createdAt
            contribution.createdBy shouldBe expected.createdBy
            contribution.visibility shouldBe Visibility.DEFAULT
            contribution.unlistedBy shouldBe expected.unlistedBy
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
