package org.orkg.dataimport.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.common.ContributorId
import org.orkg.common.md5
import org.orkg.dataimport.domain.rdf.RdfFile
import org.orkg.dataimport.output.RdfFileRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest

private val fabricator = Fabrikate(
    FabricatorConfig(
        collectionSizes = 12..12,
        nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull, // FIXME: because "id" is nullable
    )
        .withStandardMappings()
        .withGraphMappings(),
)

interface RdfFileRepositoryContracts {
    val repository: RdfFileRepository

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    fun `Saving an rdf file, saves and loads all properties correctly`() {
        val expected: RdfFile = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual.shouldNotBeNull().asClue {
            it.id shouldBe expected.id
            it.name shouldBe expected.name
            it.baseUri shouldBe expected.baseUri
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
    fun `Finding several rdf files by created by, returns the correct results`() {
        val contributorId = ContributorId("993ff403-8a1b-4d2f-99eb-b84a15434ef1")
        val expected = fabricator.random<List<RdfFile>>()
            .map { it.copy(createdBy = contributorId) }
        expected.forEach(repository::save)

        fabricator.random<List<RdfFile>>().forEach(repository::save)

        val result = repository.findAllByCreatedBy(
            createdBy = contributorId,
            pageable = PageRequest.of(0, 15),
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
    fun `Checking the existence of an rdf files by data md5, returns the correct results`() {
        val rdfFile = fabricator.random<RdfFile>()
        val hash = rdfFile.data.md5

        repository.existsByDataMD5(hash) shouldBe false

        repository.save(rdfFile)

        repository.existsByDataMD5(hash) shouldBe true
    }

    @Test
    fun `Deleting an rdf file by id, deletes the rdfFile`() {
        val rdfFile = fabricator.random<RdfFile>()
        repository.save(rdfFile)

        repository.count() shouldBe 1

        repository.deleteById(rdfFile.id)

        repository.findById(rdfFile.id).isPresent shouldBe false
    }

    @Test
    fun `Counting rdf files, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<RdfFile>())
        }

        repository.count() shouldBe 3
    }
}
