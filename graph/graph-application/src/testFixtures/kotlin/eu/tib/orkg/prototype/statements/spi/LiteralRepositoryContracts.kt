package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.statements.domain.model.Literal
import eu.tib.orkg.prototype.statements.domain.model.SearchString
import eu.tib.orkg.prototype.statements.domain.model.ThingId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import eu.tib.orkg.prototype.random
import eu.tib.orkg.prototype.statements.testing.fixtures.createLiteral
import eu.tib.orkg.prototype.withCustomMappings
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <R : LiteralRepository> literalRepositoryContract(
    repository: R
) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    describe("saving a literal") {
        it("saves and loads all properties correctly") {
            val expected: Literal = fabricator.random()
            repository.save(expected)

            val actual = repository.findById(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.datatype shouldBe expected.datatype
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
            }
        }
        it("updates an already existing literal") {
            val original: Literal = fabricator.random()
            repository.save(original)
            val found = repository.findById(original.id).get()
            val modifiedLabel = "modified label"
            val modified = found.copy(label = modifiedLabel)
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findById(original.id).get().label shouldBe modifiedLabel
        }
    }

    describe("finding several literals") {
        context("by label") {
            val expectedCount = 3
            val label = "label-to-find"
            val literals = fabricator.random<List<Literal>>().toMutableList()
            (0 until 3).forEach {
                literals[it] = literals[it].copy(label = label)
            }

            val expected = literals.take(expectedCount)

            context("with exact matching") {
                literals.forEach(repository::save)
                val result = repository.findAllByLabel(
                    SearchString.of(label, exactMatch = true),
                    PageRequest.of(0, 5)
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expectedCount
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expectedCount
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("with fuzzy matching") {
                literals.forEach(repository::save)
                val result = repository.findAllByLabel(
                    SearchString.of("label find", exactMatch = false),
                    PageRequest.of(0, 5)
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expectedCount
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expectedCount
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
        }
        context("with filters") {
            context("using no parameters") {
                val resources = fabricator.random<Literal>(10)
                resources.forEach(repository::save)

                val pageable = PageRequest.of(0, 10)
                val result = repository.findAllWithFilters(pageable = pageable)

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe resources.size
                    result.content shouldContainAll resources
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe resources.size
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("using several parameters") {
                val resources = fabricator.random<MutableList<Literal>>()
                resources.forEach(repository::save)

                val expected = listOf(resources[0])
                val pageable = PageRequest.of(0, 10)
                val result = repository.findAllWithFilters(
                    createdBy = resources.first().createdBy,
                    createdAt = resources.first().createdAt,
                    pageable = pageable
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.size
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("using sorting parameters") {
                val resources = fabricator.random<List<Literal>>()
                resources.forEach(repository::save)

                val expected = resources.sortedByDescending { it.createdBy.value.toString() }.take(10)
                val pageable = PageRequest.of(0, 10, Sort.by("created_by").descending())
                val result = repository.findAllWithFilters(pageable = pageable)

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.size
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 2
                    result.totalElements shouldBe resources.size
                }
                it("sorts the results by descending created by") {
                    result.content.zipWithNext { a, b ->
                        a.createdBy.value.toString() shouldBeGreaterThan b.createdBy.value.toString()
                    }
                }
            }
        }
    }

    it("delete all literals") {
        repeat(3) {
            repository.save(createLiteral(id = ThingId("$it")))
        }
        // LiteralRepository has no count method
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 3
        repository.deleteAll()
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 0
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank")  {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
            it("that is prefixed with 'L'") {
                repository.nextIdentity().value[0] shouldBe 'L'
            }
        }
        it("returns an id that is not yet in the repository") {
            val literal = createLiteral(id = repository.nextIdentity())
            repository.save(literal)
            val id = repository.nextIdentity()
            repository.findById(id).isPresent shouldBe false
        }
    }
}
