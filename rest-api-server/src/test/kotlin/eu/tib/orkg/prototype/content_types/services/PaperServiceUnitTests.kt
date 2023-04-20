package eu.tib.orkg.prototype.content_types.services

import eu.tib.orkg.prototype.statements.spi.ResourceRepository
import eu.tib.orkg.prototype.statements.spi.StatementRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Page

class PaperServiceUnitTests {
    private val resourceRepository: ResourceRepository = mockk()
    private val statementRepository: StatementRepository = mockk()

    private val service = PaperService(resourceRepository, statementRepository)

    @Test
    fun `given a paper`() {
        every { resourceRepository.findAllByClass(Classes.paper, any()) } returns Page.empty()

        service.findAll(PageRequests.ALL)

        verify(exactly = 1) { resourceRepository.findAllByClass(Classes.paper, any()) }
    }
}
