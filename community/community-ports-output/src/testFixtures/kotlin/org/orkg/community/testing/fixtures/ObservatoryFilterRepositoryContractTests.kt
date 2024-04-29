package org.orkg.community.testing.fixtures

import org.orkg.community.output.ObservatoryFilterRepository
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.auth.output.UserRepository
import org.orkg.auth.testing.fixtures.createUser
import org.orkg.common.OrganizationId
import org.orkg.community.domain.ObservatoryFilter
import org.orkg.community.domain.ObservatoryFilterId
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.springframework.data.domain.PageRequest

interface ObservatoryFilterRepositoryContractTests {
    val repository: ObservatoryFilterRepository
    val observatoryRepository: ObservatoryRepository
    val organizationRepository: OrganizationRepository
    val userRepository: UserRepository

    @Test
    fun `successfully restores all properties after saving`() {
        val expected = createObservatoryFilter(
            exact = true,
            featured = true
        )

        saveObservatoryFilter(expected)

        val actual = repository.findById(expected.id)

        actual shouldNotBe null
        actual.isPresent shouldBe true
        actual.get() shouldNotBe null
        actual.get().asClue {
            it.id shouldBe expected.id
            it.observatoryId shouldBe expected.observatoryId
            it.label shouldBe expected.label
            it.createdBy shouldBe expected.createdBy
            it.createdAt shouldBe expected.createdAt
            it.path shouldBe expected.path
            it.range shouldBe expected.range
            it.exact shouldBe expected.exact
            it.featured shouldBe expected.featured
        }
    }

    @Test
    fun `when searching for an observatory filter, and the observatory filter is not in the repository, an empty result is returned from the repository`() {
        val result = repository.findById(ObservatoryFilterId(UUID.randomUUID()))
        result.isPresent shouldBe false
    }

    @Test
    fun `given a new id is requested, it should be different`() {
        val id1 = repository.nextIdentity()
        val id2 = repository.nextIdentity()
        id1 shouldNotBe id2
    }

    @Test
    fun `given several observatory filters, when searched by observatory id, it returns the correct result`() {
        val filter1 = createObservatoryFilter()
        val filter2 = createObservatoryFilter()

        saveObservatoryFilter(filter1)
        saveObservatoryFilter(filter2)

        val result = repository.findAllByObservatoryId(filter1.observatoryId, PageRequest.of(0, Int.MAX_VALUE))

        result.totalElements shouldBe 1
        result shouldContainExactly setOf(filter1)
    }

    private fun saveObservatoryFilter(observatoryFilter: ObservatoryFilter) {
        // Ensure user exists in the database
        val createdBy = observatoryFilter.createdBy.value
        if (userRepository.findById(createdBy).isEmpty) {
            userRepository.save(createUser(createdBy))
        }
        // Ensure organization exists in the database
        val organizationId = OrganizationId(UUID.randomUUID())
        val organization = createOrganization(
            id = organizationId,
            displayId = "displayId_${organizationId.value}",
            createdBy = observatoryFilter.createdBy // The user has to exists in the database because of constraints, so we reuse the observatory filter creator
        )
        organizationRepository.save(organization)
        // Ensure observatory exists in the database
        val observatory = createObservatory(
            id = observatoryFilter.observatoryId,
            organizationIds = setOf(organizationId),
            displayId = "displayId_${observatoryFilter.observatoryId}"
        )
        observatoryRepository.save(observatory)
        // Save the filter
        repository.save(observatoryFilter)
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
