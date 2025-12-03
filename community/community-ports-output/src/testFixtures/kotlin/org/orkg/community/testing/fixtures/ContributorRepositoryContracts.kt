package org.orkg.community.testing.fixtures

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.community.output.ContributorRepository
import org.springframework.data.domain.PageRequest

interface ContributorRepositoryContracts {
    val repository: ContributorRepository

    @Test
    fun `saving a contributor, saves and loads all properties correctly`() {
        val expected = createContributor()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.name shouldBe expected.name
            it.organizationId shouldBe expected.organizationId
            it.observatoryId shouldBe expected.observatoryId
            it.emailHash shouldBe expected.emailHash
            it.isCurator shouldBe expected.isCurator
            it.isAdmin shouldBe expected.isAdmin
            // getter
            it.gravatarId shouldBe expected.gravatarId
            it.avatarURL shouldBe expected.avatarURL
        }
    }

    @Test
    fun `saving a contributor, updates an already existing contributor`() {
        val original = createContributor()
        repository.save(original)

        val found = repository.findById(original.id).orElse(null)
        val modified = found.copy(
            name = "New name",
        )
        repository.save(modified)

        repository.count() shouldBe 1

        val actual = repository.findById(original.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.name shouldBe modified.name
        }
    }

    @Test
    fun `deleting a contributor by id, deletes the contributor`() {
        val contributor = createContributor()
        repository.save(contributor)

        repository.count() shouldBe 1

        repository.deleteById(contributor.id)

        repository.findById(contributor.id).isPresent shouldBe false
    }

    @Test
    fun `finding several contributors, by ids, returns the correct results`() {
        val contributors = listOf(
            createContributor(ContributorId("a4e7574c-326c-4b93-bb61-7c46e6f003b0")),
            createContributor(ContributorId("743398a7-8bf2-44a6-8561-8c564bb59a0b")),
            createContributor(ContributorId("4c53b8ab-42cc-487d-8f48-6ad430101eca")),
        )

        contributors.forEach(repository::save)

        val expected = contributors.take(2)
        val ids = expected.map { it.id }

        repository.findAllById(ids) shouldBe expected
    }

    @Test
    fun `finding several contributors, without filters, returns the correct results`() {
        val contributors = listOf(
            createContributor(ContributorId("a4e7574c-326c-4b93-bb61-7c46e6f003b0")),
            createContributor(ContributorId("743398a7-8bf2-44a6-8561-8c564bb59a0b")),
            createContributor(ContributorId("4c53b8ab-42cc-487d-8f48-6ad430101eca")),
        )

        contributors.forEach(repository::save)

        val expected = contributors.take(2)
        val result = repository.findAll(
            pageable = PageRequest.of(0, 2)
        )

        // returns the correct result
        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected
        // pages the result correctly
        result.size shouldBe 2
        result.number shouldBe 0
        result.totalPages shouldBe 2
        result.totalElements shouldBe contributors.size
        // sorts the results by creation date by default
        result.content.zipWithNext { a, b ->
            a.joinedAt shouldBeLessThanOrEqualTo b.joinedAt
        }
    }

    @Test
    fun `finding several contributors, by display name, returns the correct results`() {
        val contributors = listOf(
            createContributor(ContributorId("a4e7574c-326c-4b93-bb61-7c46e6f003b0"), name = "John Doe"),
            createContributor(ContributorId("743398a7-8bf2-44a6-8561-8c564bb59a0b"), name = "Jane Doe"),
            createContributor(ContributorId("4c53b8ab-42cc-487d-8f48-6ad430101eca"), name = "Jane Roe"),
        )

        contributors.forEach(repository::save)

        val expected = contributors.take(2)
        val result = repository.findAll(
            pageable = PageRequest.of(0, 2),
            label = "Doe"
        )

        // returns the correct result
        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected
        // pages the result correctly
        result.size shouldBe 2
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size
        // sorts the results by creation date by default
        result.content.zipWithNext { a, b ->
            a.joinedAt shouldBeLessThanOrEqualTo b.joinedAt
        }
    }

    @Test
    fun `deleting all contributors`() {
        repeat(3) {
            repository.save(createContributor(ContributorId("824e21b5-5df6-44c7-b2db-5929598f739$it")))
        }

        repository.count() shouldBe 3
        repository.deleteAll()
        repository.count() shouldBe 0
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
