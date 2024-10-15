package org.orkg.community.testing.fixtures

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.community.domain.Observatory
import org.orkg.community.output.ObservatoryRepository
import org.orkg.community.output.OrganizationRepository
import org.springframework.data.domain.PageRequest

interface ObservatoryRepositoryContractTests {
    val repository: ObservatoryRepository
    val organizationRepository: OrganizationRepository

    @Test
    fun `successfully restores all properties after saving`() {
        val expected = createObservatory(
            organizationIds = setOf(
                OrganizationId("6b9737ca-0cf4-4265-b6a3-316506587906"),
                OrganizationId("fd8cc4bf-5862-43cb-96ea-2a76db509ad7")
            ),
            sustainableDevelopmentGoals = setOf(ThingId("SDG1"))
        )
        expected.createOrganizations()
        repository.save(expected)

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
            it.sustainableDevelopmentGoals shouldBe expected.sustainableDevelopmentGoals
        }
    }

    @Test
    fun `Saving an observatory, updates an already existing observatory`() {
        val observatory = createObservatory(
            organizationIds = setOf(
                OrganizationId("6b9737ca-0cf4-4265-b6a3-316506587906"),
                OrganizationId("fd8cc4bf-5862-43cb-96ea-2a76db509ad7")
            ),
            sustainableDevelopmentGoals = setOf(ThingId("SDG1"))
        )
        observatory.createOrganizations()
        repository.save(observatory)

        val updated = repository.findById(observatory.id).get().copy(
            name = "new name",
            organizationIds = setOf(
                OrganizationId("6b9737ca-0cf4-4265-b6a3-316506587906")
            ),
            description = "new description",
            sustainableDevelopmentGoals = setOf(ThingId("SDG1"), ThingId("SDG2"))
        )
        repository.save(updated)

        val actual = repository.findById(observatory.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe updated.id
            it.name shouldBe updated.name
            it.description shouldBe updated.description
            it.researchField shouldBe updated.researchField
            it.organizationIds.size shouldBe updated.organizationIds.size
            it.organizationIds shouldContainAll updated.organizationIds
            it.displayId shouldBe updated.displayId
            it.sustainableDevelopmentGoals shouldBe updated.sustainableDevelopmentGoals
        }
    }

    @Test
    fun `When searching several observatories by organization id, it returns the correct result`() {
        val expected = createObservatory(
            organizationIds = setOf(
                OrganizationId("6b9737ca-0cf4-4265-b6a3-316506587906"),
                OrganizationId("fd8cc4bf-5862-43cb-96ea-2a76db509ad7")
            )
        )
        expected.createOrganizations()
        repository.save(expected)

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
        val expected = createObservatory()
        repository.save(expected)

        val actual = repository.findByName(expected.name).orElse(null)

        actual shouldNotBe null
        actual shouldBe expected
    }

    @Test
    fun `When searching an observatory by its display Id, it returns the correct result`() {
        val expected = createObservatory()
        repository.save(expected)

        val actual = repository.findByDisplayId(expected.displayId).orElse(null)

        actual shouldNotBe null
        actual shouldBe expected
    }

    @Test
    fun `When searching several observatories by research field id, it returns the correct result`() {
        val expected = createObservatory()
        repository.save(expected)

        val actual = repository.findAllByResearchField(expected.researchField!!, PageRequest.of(0, 5))

        actual shouldNotBe null
        actual.content shouldContainAll setOf(expected)
        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 1
        actual.totalElements shouldBe 1
    }

    @Test
    fun `When retrieving several observatories, it returns the correct result`() {
        val expected = createObservatory()
        repository.save(expected)

        val actual = repository.findAll(PageRequest.of(0, 5))

        actual shouldNotBe null
        actual.content shouldContainAll setOf(expected)
        actual.size shouldBe 5
        actual.number shouldBe 0
        actual.totalPages shouldBe 1
        actual.totalElements shouldBe 1
    }

    @Test
    fun `When retrieving several research fields, it returns the correct result`() {
        val nullObservatory = Observatory(
            id = ObservatoryId(UUID.randomUUID()),
            name = "zero",
            description = "desc",
            researchField = null,
            displayId = "displayId"
        )
        val observatories = (0..3).map {
            Observatory(
                id = ObservatoryId(UUID.randomUUID()),
                name = "$it",
                description = "desc",
                researchField = ThingId("R${it.coerceAtMost(2)}"),
                displayId = "displayId$it"
            )
        }
        repository.save(nullObservatory)
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
        val observatories = (0..3).map {
            Observatory(
                id = ObservatoryId(UUID.randomUUID()),
                name = "ABC$it",
                description = "desc",
                researchField = ThingId("R${it.coerceAtMost(2)}"),
                organizationIds = emptySet(),
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

    private fun Observatory.createOrganizations() {
        organizationIds.forEach {
            val organization = createOrganization(id = it, displayId = it.value.toString())
            organizationRepository.save(organization)
        }
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
