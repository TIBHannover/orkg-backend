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
import org.orkg.dataimport.domain.rdf.RdfPredicate
import org.orkg.dataimport.domain.testing.fixtures.withDataImportMappings
import org.orkg.dataimport.output.RdfPredicateRepository
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

interface RdfPredicateRepositoryContracts {
    val repository: RdfPredicateRepository

    @AfterEach
    fun cleanup() {
        repository.deleteAll()
    }

    @Test
    fun `Saving an rdf predicate, saves and loads all properties correctly`() {
        val expected: RdfPredicate = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual.shouldNotBeNull().asClue {
            it.id shouldBe expected.id
            it.rdfFileId shouldBe expected.rdfFileId
            it.iri shouldBe expected.iri
            it.label shouldBe expected.label
        }
    }

    @Test
    fun `Finding several rdf predicates by rdf file id, returns the correct results`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val expected = fabricator.random<List<RdfPredicate>>()
            .map { it.copy(rdfFileId = rdfFileId) }
        expected.forEach(repository::save)

        fabricator.random<List<RdfPredicate>>().forEach(repository::save)

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

        result.content.zipWithNext { a, b ->
            a.iri.toString().lowercase() shouldBeLessThan b.iri.toString().lowercase()
        }
    }

    @Test
    fun `Deleting an rdf predicate by id, deletes the rdf predicate`() {
        val rdfPredicate = fabricator.random<RdfPredicate>()
        repository.save(rdfPredicate)

        repository.count() shouldBe 1

        repository.deleteById(rdfPredicate.id)

        repository.findById(rdfPredicate.id).isPresent shouldBe false
    }

    @Test
    fun `Deleting all rdf predicates by rdf file id, deletes the correct rdf predicates`() {
        val rdfFileId = UUID.fromString("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val predicates = fabricator.random<List<RdfPredicate>>()
            .map { it.copy(rdfFileId = rdfFileId) }
        predicates.forEach(repository::save)

        val expected = fabricator.random<List<RdfPredicate>>()
        expected.forEach(repository::save)

        repository.deleteAllByRdfFileId(rdfFileId)

        val actual = repository.findAll()
        actual.count() shouldBe expected.size
        actual shouldContainAll expected
    }

    @Test
    fun `Counting rdf predicates, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<RdfPredicate>())
        }

        repository.count() shouldBe 3
    }
}
