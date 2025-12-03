package org.orkg.contenttypes.domain

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.MockkBaseTest
import org.orkg.contenttypes.output.LegacyResearchFieldRepository
import org.orkg.contenttypes.output.ResearchFieldHierarchyRepository
import org.orkg.graph.domain.Classes
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.testing.pageOf
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import java.util.Optional

internal class ResearchFieldHierarchyServiceUnitTest : MockkBaseTest {
    private val repository: ResearchFieldHierarchyRepository = mockk()
    private val researchFieldRepository: LegacyResearchFieldRepository = mockk()
    private val service = ResearchFieldHierarchyService(repository, researchFieldRepository)

    @Test
    fun `given a research field id, when searching for its subfields, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findAllChildrenByAncestorId(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
    }

    @Test
    fun `given a research field id, when the subfields are fetched, it returns success`() {
        val subfieldId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findAllChildrenByAncestorId(subfieldId, pageable) } returns Page.empty()

        service.findAllChildrenByAncestorId(subfieldId, pageable)

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
        verify(exactly = 1) { repository.findAllChildrenByAncestorId(subfieldId, pageable) }
    }

    @Test
    fun `given a research field id, when searching for its parent research fields, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findAllParentsByChildId(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
    }

    @Test
    fun `given a research field id, when the parent research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findAllParentsByChildId(subfieldId, any()) } returns pageOf(createResearchField(ThingId("parent")))

        service.findAllParentsByChildId(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
        verify(exactly = 1) { repository.findAllParentsByChildId(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when the non-existing parent research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findAllParentsByChildId(subfieldId, any()) } returns Page.empty()

        service.findAllParentsByChildId(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
        verify(exactly = 1) { repository.findAllParentsByChildId(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when searching for its root research fields, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findAllRootsByDescendantId(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
    }

    @Test
    fun `given a research field id, when the root research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findAllRootsByDescendantId(subfieldId, any()) } returns pageOf(createResearchField(ThingId("root")))

        service.findAllRootsByDescendantId(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
        verify(exactly = 1) { repository.findAllRootsByDescendantId(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when the non-existing root research fields are fetched, it returns success`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findAllRootsByDescendantId(subfieldId, any()) } returns Page.empty()

        service.findAllRootsByDescendantId(subfieldId, PageRequest.of(0, 5))

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
        verify(exactly = 1) { repository.findAllRootsByDescendantId(subfieldId, any()) }
    }

    @Test
    fun `given a research field id, when searching for its research field hierarchy, when the research field is not found, then an exception is thrown`() {
        val subfieldId = ThingId("child")

        every { researchFieldRepository.findById(subfieldId) } returns Optional.empty()

        val exception = assertThrows<ResearchFieldNotFound> {
            service.findResearchFieldHierarchyByResearchFieldId(subfieldId, PageRequest.of(0, 5))
        }
        assertThat(exception.message).isEqualTo(ResearchFieldNotFound(subfieldId).message)

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
    }

    @Test
    fun `given a research field id, when searching for its research field hierarchy, it returns success`() {
        val subfieldId = ThingId("child")
        val pageable = PageRequest.of(0, 5)

        every { researchFieldRepository.findById(subfieldId) } returns Optional.of(createResearchField(subfieldId))
        every { repository.findResearchFieldHierarchyByResearchFieldId(subfieldId, pageable) } returns pageOf()

        service.findResearchFieldHierarchyByResearchFieldId(subfieldId, pageable)

        verify(exactly = 1) { researchFieldRepository.findById(subfieldId) }
        verify(exactly = 1) { repository.findResearchFieldHierarchyByResearchFieldId(subfieldId, pageable) }
    }

    private fun createResearchField(id: ThingId) =
        createResource(id = id, classes = setOf(Classes.researchField))
}
