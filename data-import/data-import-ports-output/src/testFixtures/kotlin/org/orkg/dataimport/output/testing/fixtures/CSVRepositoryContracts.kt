package org.orkg.dataimport.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.dataimport.domain.csv.CSV
import org.orkg.dataimport.output.CSVRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest

private val fabricator = Fabrikate(
    FabricatorConfig(
        collectionSizes = 12..12,
        nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
    )
        .withStandardMappings()
        .withGraphMappings()
)

interface CSVRepositoryContracts {
    val repository: CSVRepository

    @Test
    fun `Saving a csv, saves and loads all properties correctly`() {
        val expected: CSV = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.name shouldBe expected.name
            it.type shouldBe expected.type
            it.format shouldBe expected.format
            it.state shouldBe expected.state
            it.validationJobId shouldBe expected.validationJobId
            it.importJobId shouldBe expected.importJobId
            it.data shouldBe expected.data
            it.createdBy shouldBe expected.createdBy
            it.createdAt shouldBe expected.createdAt
        }
    }

    @Test
    fun `Finding several csvs by created by, returns the correct results`() {
        val contributorId = ContributorId("993ff403-8a1b-4d2f-99eb-b84a15434ef1")
        val expected = fabricator.random<List<CSV>>()
            .map { it.copy(createdBy = contributorId) }
        expected.forEach(repository::save)

        fabricator.random<List<CSV>>().forEach(repository::save)

        val result = repository.findAllByCreatedBy(
            createdBy = contributorId,
            pageable = PageRequest.of(0, 15)
        )

        result shouldNotBe null
        result.content shouldNotBe null
        result.content.size shouldBe expected.size
        result.content shouldContainAll expected

        result.size shouldBe 15
        result.number shouldBe 0
        result.totalPages shouldBe 1
        result.totalElements shouldBe expected.size

        result.content.zipWithNext { a, b ->
            a.createdAt shouldBeLessThan b.createdAt
        }
    }

    @Test
    fun `Checking the existence of a csvs by data md5, returns the correct results`() {
        val csv = fabricator.random<CSV>()
        val hash = csv.data.md5

        repository.existsByDataMD5(hash) shouldBe false

        repository.save(csv)

        repository.existsByDataMD5(hash) shouldBe true
    }

    @Test
    fun `Deleting a csv by id, deletes the csv`() {
        val csv = fabricator.random<CSV>()
        repository.save(csv)

        repository.count() shouldBe 1

        repository.deleteById(csv.id)

        repository.findById(csv.id).isPresent shouldBe false
    }

    @Test
    fun `Counting csvs, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<CSV>())
        }

        repository.count() shouldBe 3
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
