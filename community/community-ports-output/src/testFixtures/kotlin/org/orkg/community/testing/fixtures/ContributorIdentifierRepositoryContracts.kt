package org.orkg.community.testing.fixtures

import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.ORCID
import org.orkg.community.output.ContributorIdentifierRepository
import org.orkg.community.output.ContributorRepository
import org.springframework.data.domain.PageRequest

interface ContributorIdentifierRepositoryContracts {
    val repository: ContributorIdentifierRepository
    val contributorRepository: ContributorRepository

    @Test
    fun `Saving a contributor identifier, saves and loads all properties correctly`() {
        val expected = createContributorIdentifier()
        contributorRepository.save(createContributor(expected.contributorId))
        repository.save(expected)

        val actual = repository.findByContributorIdAndValue(expected.contributorId, expected.value.value)

        actual.isPresent shouldBe true
        actual.get().asClue {
            it.contributorId shouldBe expected.contributorId
            it.type shouldBe expected.type
            it.value.value shouldBe expected.value.value // FIXME: Identifiers are not comparable
            it.createdAt shouldBe expected.createdAt
        }
    }

    @Test
    fun `Finding several contributor identifiers, by contributor id, returns the correct result`() {
        val identifiers = listOf(
            createContributorIdentifier(value = ORCID.of("0000-0001-5109-3700")),
            createContributorIdentifier(value = ORCID.of("0000-0001-5109-3702")),
            createContributorIdentifier(ContributorId("4c53b8ab-42cc-487d-8f48-6ad430101eca")),
        )
        identifiers.forEach {
            contributorRepository.save(createContributor(it.contributorId))
            repository.save(it)
        }
        val expected = identifiers.take(2)

        val result = repository.findAllByContributorId(
            contributorId = identifiers.first().contributorId,
            pageable = PageRequest.of(0, 5)
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 5
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.createdAt shouldBeLessThanOrEqualTo b.createdAt
        }
    }

    @Test
    fun `Deleting a contributor identifier by contributor id and value, deletes the contributor identifier`() {
        val identifier = createContributorIdentifier()
        contributorRepository.save(createContributor(identifier.contributorId))
        repository.save(identifier)

        repository.count() shouldBe 1

        repository.deleteByContributorIdAndValue(identifier.contributorId, identifier.value.value)

        repository.findByContributorIdAndValue(identifier.contributorId, identifier.value.value).isPresent shouldBe false
    }

    @Test
    fun `Deleting all contributor identifiers`() {
        repeat(3) {
            val contributorId = ContributorId("824e21b5-5df6-44c7-b2db-5929598f739$it")
            contributorRepository.save(createContributor(contributorId))
            repository.save(createContributorIdentifier(contributorId))
        }

        repository.count() shouldBe 3
        repository.deleteAll()
        repository.count() shouldBe 0
    }

    @AfterEach
    fun cleanUp() {
        repository.deleteAll()
        contributorRepository.deleteAll()
    }
}
