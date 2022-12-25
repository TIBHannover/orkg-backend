package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.statements.domain.model.Class
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.data.domain.PageRequest

fun <R : ClassRepository> classRepositoryContract(repository: R) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    )

    describe("saving a class") {
        it("saves and loads all properties correctly") {
            val expected: Class = fabricator.random()
            repository.save(expected)

            val actual = repository.findByClassId(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.uri shouldBe expected.uri
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it.createdBy shouldBe expected.createdBy
                it.createdAt shouldBe expected.createdAt
                it._class shouldBe "class"
                it.thingId shouldBe expected.thingId
                // it.description shouldBe it.description
            }
        }
    }
    context("loading several classes") {
        val classes = fabricator.random<List<Class>>()
        classes.forEach(repository::save)

        // Explicitly requesting second page here
        val result = repository.findAll(PageRequest.of(1, 5))

        it("pages the results correctly") {
            result.size shouldBe 5
            result.number shouldBe 1 // 0-indexed
            result.totalPages shouldBe 3
            result.totalElements shouldBe 12
        }
        xit("sorts the results by creation date by default") {
            result.content.zipWithNext { a, b ->
                a.createdAt shouldBeLessThan b.createdAt
            }
        }
    }
}
