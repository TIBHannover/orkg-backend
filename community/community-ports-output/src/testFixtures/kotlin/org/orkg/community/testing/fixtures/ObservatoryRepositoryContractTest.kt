package org.orkg.community.testing.fixtures

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.auth.output.UserRepository
import org.orkg.auth.testing.fixtures.createUser
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.adapter.output.jpa.internal.OrganizationEntity
import org.orkg.community.adapter.output.jpa.internal.PostgresOrganizationRepository
import org.orkg.community.domain.Observatory
import org.orkg.community.domain.Organization
import org.orkg.community.output.ObservatoryRepository
import org.springframework.data.domain.PageRequest

interface ObservatoryRepositoryContractTest {
    val repository: ObservatoryRepository
    val organizationRepository: PostgresOrganizationRepository
    val userRepository: UserRepository

    @Test
    fun `successfully restores all properties after saving`() {
        val expected = createObservatory(organizations = 2)

        val actual = repository.findById(expected.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.name shouldBe expected.name
            it.description shouldBe expected.description
            it.researchField shouldBe expected.researchField
            it.organizationIds.size shouldBe expected.organizationIds.size
            it.organizationIds shouldContainAll expected.organizationIds
            it.displayId shouldBe expected.displayId
        }
    }

    @Test
    fun `When searching several observatories by organization id, it returns the correct result`() {
        val expected = createObservatory(organizations = 2)

        val actual = repository.findAllByOrganizationId(expected.organizationIds.first(), PageRequest.of(0, 5))
        actual.content shouldNotBe null
        actual.content shouldContainAll setOf(expected)
        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 1
        actual.totalElements shouldBe 1
    }

    @Test
    fun `When searching an observatory by its name, it returns the correct result`() {
        val expected = createObservatory(organizations = 2)

        val actual = repository.findByName(expected.name).orElse(null)

        actual shouldNotBe null
        actual shouldBe expected
    }

    @Test
    fun `When searching an observatory by its display Id, it returns the correct result`() {
        val expected = createObservatory(organizations = 2)

        val actual = repository.findByDisplayId(expected.displayId).orElse(null)

        actual shouldNotBe null
        actual shouldBe expected
    }

    @Test
    fun `When searching several observatories by research field id, it returns the correct result`() {
        val expected = createObservatory(organizations = 2)

        val actual = repository.findAllByResearchField(expected.researchField!!, PageRequest.of(0, 5))

        actual shouldNotBe null
        actual.content shouldContainAll setOf(expected)
        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 1
        actual.totalElements shouldBe 1
    }

    @Test
    fun `When searching observatories by incorrect display id, it returns the incorrect result`() {
        val expected = createObservatory(organizations = 1)
        val actual = repository.findByDisplayId("test observatory").orElse(null)

        actual shouldNotBe expected
    }

    @Test
    fun `When searching observatories by incorrect research field id, it returns the incorrect result`() {
        createObservatory(organizations = 1)
        val actual = repository.findAllByResearchField(ThingId("R1"), PageRequest.of(0, 5))

        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 0
        actual.totalElements shouldBe 0
    }

    @Test
    fun `When retrieving several observatories, it returns the correct result`() {
        val expected = createObservatory(2)
        val actual = repository.findAll(PageRequest.of(0, 5))

        actual shouldNotBe null
        actual.content shouldContainAll setOf(expected)
        actual.content.first().organizationIds.size shouldBe 2
        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 1
        actual.totalElements shouldBe 1
    }

    @Test
    fun `When retrieving several research fields, it returns the correct result`() {
        val organization = organizationRepository.createOrganization()
        val nullObservatory = Observatory(
            id = ObservatoryId(UUID.randomUUID()),
            name = "zero",
            description = "desc",
            researchField = null,
            organizationIds = setOf(organization),
            displayId = "displayId"
        )
        val observatories = (0..3).map {
            Observatory(
                id = ObservatoryId(UUID.randomUUID()),
                name = "$it",
                description = "desc",
                researchField = ThingId("R${it.coerceAtMost(2)}"),
                organizationIds = setOf(organization),
                displayId = "displayId$it"
            )
        } + nullObservatory
        observatories.forEach(repository::save)

        val expected = observatories.mapNotNull { it.researchField }.toSet()
        val actual = repository.findAllResearchFields(PageRequest.of(0, 5))

        actual shouldNotBe null
        actual.content shouldNotBe null
        actual.content.size shouldBe expected.size
        actual.content shouldContainAll expected
        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 1
        actual.totalElements shouldBe expected.size
    }

    @Test
    fun `When searching several observatories by name containing, it returns the correct result`() {
        val organization = organizationRepository.createOrganization()
        val observatories = (0..3).map {
            Observatory(
                id = ObservatoryId(UUID.randomUUID()),
                name = "ABC$it",
                description = "desc",
                researchField = ThingId("R${it.coerceAtMost(2)}"),
                organizationIds = setOf(organization),
                displayId = "displayId$it"
            )
        }
        observatories.forEach(repository::save)

        val expected = observatories.filter { it.name.contains("c1", ignoreCase = true) }
        val actual = repository.findAllByNameContains("c1", PageRequest.of(0, 5))

        actual shouldNotBe null
        actual.content shouldNotBe null
        actual.content.size shouldBe expected.size
        actual.content shouldContainAll expected
        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 1
        actual.totalElements shouldBe expected.size
    }

    private fun createObservatory(organizations: Int = 1): Observatory {
        val organizationIds = (0 until organizations)
            .map { organizationRepository.createOrganization() }
            .toSet()
        val observatory = createObservatory(organizationIds)
        repository.save(observatory)
        return observatory
    }

    private fun PostgresOrganizationRepository.createOrganization(): OrganizationId {
        val organizationId = OrganizationId(UUID.randomUUID())
        val organization = org.orkg.community.testing.fixtures.createOrganization().copy(
            id = organizationId,
            displayId = "displayId${organizationId.value}"
        )
        val createdBy = organization.createdBy!!.value
        if (userRepository.findById(createdBy).isEmpty) {
            userRepository.save(createUser(createdBy))
        }
        this.save(toOrganizationEntity(organization))
        return organizationId
    }

    private fun PostgresOrganizationRepository.toOrganizationEntity(organization: Organization): OrganizationEntity =
        findById(organization.id!!.value).orElse(OrganizationEntity()).apply {
            id = organization.id!!.value
            name = organization.name
            createdBy = organization.createdBy?.value
            url = organization.homepage
            displayId = organization.displayId
            type = organization.type
            logoId = organization.logoId?.value
        }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}