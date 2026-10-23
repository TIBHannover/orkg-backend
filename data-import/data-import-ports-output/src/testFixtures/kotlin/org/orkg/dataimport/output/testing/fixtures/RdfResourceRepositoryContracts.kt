package org.orkg.dataimport.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.orkg.dataimport.domain.rdf.RdfResource
import org.orkg.dataimport.domain.testing.fixtures.withDataImportMappings
import org.orkg.dataimport.output.RdfResourceRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.springframework.data.domain.PageRequest
import java.util.UUID

private val fabricator = Fabrikate(
    FabricatorConfig(
        collectionSizes = 12..12,
        nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull, // FIXME: because "id" is nullable
    )
        .withStandardMappings()
        .withGraphMappings()
        .withDataImportMappings(),
)

interface RdfResourceRepositoryContracts {
    val repository: RdfResourceRepository

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    fun `Saving an rdf resource, saves and loads all properties correctly`() {
        val expected: RdfResource = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual.shouldNotBeNull().asClue {
            it.id shouldBe expected.id
            it.rdfFileId shouldBe expected.rdfFileId
            it.iri shouldBe expected.iri
            it.label shouldBe expected.label
            it.classes shouldBe expected.classes
        }
    }

    @Test
    fun `Finding several rdf resources by rdf file id, returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val expected = fabricator.random<List<RdfResource>>()
            .map { it.copy(rdfFileId = rdfFileId) }
        expected.forEach(repository::save)

        fabricator.random<List<RdfResource>>().forEach(repository::save)

        val result = repository.findAllByRdfFileId(
            rdfFileId = rdfFileId,
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

//        NOTE: SQL sorts elements different to the JVM, making testing difficult
//        result.content.zipWithNext { a, b ->
//            a.iri.toString().lowercase() shouldBeLessThan b.iri.toString().lowercase()
//        }
    }

    @Test
    fun `Deleting an rdf resource by id, deletes the rdf resource`() {
        val rdfResource = fabricator.random<RdfResource>()
        repository.save(rdfResource)

        repository.count() shouldBe 1

        repository.deleteById(rdfResource.id)

        repository.findById(rdfResource.id).isPresent shouldBe false
    }

    @Test
    fun `Deleting all rdf resources by rdf file id, deletes the correct rdf resources`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val resources = fabricator.random<List<RdfResource>>()
            .map { it.copy(rdfFileId = rdfFileId) }
        resources.forEach(repository::save)

        val expected = fabricator.random<List<RdfResource>>()
        expected.forEach(repository::save)

        repository.deleteAllByRdfFileId(rdfFileId)

        val actual = repository.findAll()
        actual.count() shouldBe expected.size
        actual shouldContainAll expected
    }

    @Test
    fun `Counting rdf resources, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<RdfResource>())
        }

        repository.count() shouldBe 3
    }
}
