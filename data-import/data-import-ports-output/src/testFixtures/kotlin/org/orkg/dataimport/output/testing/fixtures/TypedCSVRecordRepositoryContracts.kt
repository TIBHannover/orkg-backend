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
import org.orkg.dataimport.domain.csv.CSVID
import org.orkg.dataimport.domain.csv.TypedCSVRecord
import org.orkg.dataimport.output.TypedCSVRecordRepository
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

interface TypedCSVRecordRepositoryContracts {
    val repository: TypedCSVRecordRepository

    @Test
    fun `Saving a typed csv record, saves and loads all properties correctly`() {
        val expected: TypedCSVRecord = fabricator.random()
        repository.save(expected)

        val actual = repository.findById(expected.id).orElse(null)

        actual shouldNotBe null
        actual.asClue {
            it.id shouldBe expected.id
            it.csvId shouldBe expected.csvId
            it.itemNumber shouldBe expected.itemNumber
            it.lineNumber shouldBe expected.lineNumber
            it.values shouldBe expected.values
        }
    }

    @Test
    fun `Finding several typed csv records by csv id, returns the correct results`() {
        val csvId = CSVID("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val expected = fabricator.random<List<TypedCSVRecord>>()
            .map { it.copy(csvId = csvId) }
        expected.forEach(repository::save)

        fabricator.random<List<TypedCSVRecord>>().forEach(repository::save)

        val result = repository.findAllByCSVID(
            csvId = csvId,
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
            a.itemNumber shouldBeLessThan b.itemNumber
        }
    }

    @Test
    fun `Deleting a typed csv record by id, deletes the csv record`() {
        val typedCSVRecord = fabricator.random<TypedCSVRecord>()
        repository.save(typedCSVRecord)

        repository.count() shouldBe 1

        repository.deleteById(typedCSVRecord.id)

        repository.findById(typedCSVRecord.id).isPresent shouldBe false
    }

    @Test
    fun `Deleting all typed csv records by csv id, deletes the correct typed csv records`() {
        val csvId = CSVID("568a6a5e-4479-466d-9936-f493ff58bdb5")
        val records = fabricator.random<List<TypedCSVRecord>>()
            .map { it.copy(csvId = csvId) }
        records.forEach(repository::save)

        val expected = fabricator.random<List<TypedCSVRecord>>()
        expected.forEach(repository::save)

        repository.deleteAllByCSVID(csvId)

        val actual = repository.findAll()
        actual.count() shouldBe expected.size
        actual shouldContainAll expected
    }

    @Test
    fun `Counting typed csv records, returns the correct value`() {
        repeat(3) {
            repository.save(fabricator.random<TypedCSVRecord>())
        }

        repository.count() shouldBe 3
    }

    fun cleanUpAfterEach()

    @AfterEach
    fun cleanUp() {
        cleanUpAfterEach()
    }
}
