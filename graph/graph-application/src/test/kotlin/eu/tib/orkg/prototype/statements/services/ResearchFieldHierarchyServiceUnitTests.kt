package eu.tib.orkg.prototype.statements.services

import eu.tib.orkg.prototype.statements.testing.fixtures.createResource
import eu.tib.orkg.prototype.spring.testing.fixtures.pageOf
import eu.tib.orkg.prototype.statements.application.ResearchFieldNotFound
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import eu.tib.orkg.prototype.statements.spi.ResearchFieldHierarchyRepository
import eu.tib.orkg.prototype.statements.spi.ResearchFieldRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest

class ResearchFieldHierarchyServiceUnitTests {
    private val repository: ResearchFieldHierarchyRepository = mockk()
    private val researchFieldRepository: ResearchFieldRepository = mockk()
    private val service = ResearchFieldHierarchyService(repository, researchFieldRepository)

    @Test
    fun `given a research field id, when searching for its subfields, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findChildren(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)
    }

    @Test
    fun `given a research field id, when the subfields are fetched, it returns success`() {
        val subfieldId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findChildren(subfieldId, pageable) } returns Page.empty()

        service.findChildren(subfieldId, pageable)

        verify(exactly = 1) { repository.findChildren(subfieldId, pageable) }
    }

    @Test
    fun `given a research field id, when searching for its parent research fields, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findParents(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)
    }

    @Test
    fun `given a research field id, when the parent research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findParents(subfieldId, any()) } returns pageOf(createResearchField(ThingId("parent")))

        service.findParents(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { repository.findParents(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when the non-existing parent research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findParents(subfieldId, any()) } returns Page.empty()

        service.findParents(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { repository.findParents(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when searching for its root research fields, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findRoots(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)
    }

    @Test
    fun `given a research field id, when the root research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findRoots(subfieldId, any()) } returns pageOf(createResearchField(ThingId("root")))

        service.findRoots(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { repository.findRoots(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when the non-existing root research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findRoots(subfieldId, any()) } returns Page.empty()

        service.findRoots(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { repository.findRoots(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when searching for its research field hierarchy, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findResearchFieldHierarchy(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)
    }

    @Test
    fun `given a research field id, when searching for its research field hierarchy, it returns success`() {
        val subfieldId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findResearchFieldHierarchy(subfieldId, pageable) } returns pageOf()

        service.findResearchFieldHierarchy(subfieldId, pageable)

        verify(exactly = 1) { repository.findResearchFieldHierarchy(subfieldId, pageable) }
    }

    private fun createResearchField(id: ThingId) =
        createResource(id = id, classes = setOf(ThingId("ResearchField")))
}
