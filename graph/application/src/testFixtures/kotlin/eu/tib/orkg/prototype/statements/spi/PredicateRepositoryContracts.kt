package eu.tib.orkg.prototype.statements.spi

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import eu.tib.orkg.prototype.statements.domain.model.Predicate
import eu.tib.orkg.prototype.statements.domain.model.PredicateId
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.statements.testing.createPredicate
import org.orkg.statements.testing.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <R : PredicateRepository> predicateRepositoryContract(repository: R) = describeSpec {
    beforeTest {
        repository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    describe("saving a predicate") {
        it("saves and loads all properties correctly") {
            val expected: Predicate = fabricator.random()
            repository.save(expected)

            val actual = repository.findByPredicateId(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it._class shouldBe "predicate"
                it.thingId shouldBe expected.thingId
                it.description shouldBe it.description
            }
        }
        it("updates an already existing predicate") {
            val original: Predicate = fabricator.random()
            repository.save(original)
            val found = repository.findByPredicateId(original.id).get()
            val modified = found.copy(label = "some new label, never seen before")
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findByPredicateId(original.id).get().label shouldBe "some new label, never seen before"
        }
    }

    describe("finding several predicates") {
        context("by label") {
            val expectedCount = 3
            val predicates = fabricator.random<List<Predicate>>().toMutableList()
            (0 until 3).forEach {
                predicates[it] = predicates[it].copy(label = "label to find")
            }
            predicates.forEach(repository::save)

            val expected = predicates.take(expectedCount)
            val result = repository.findAllByLabel(
                "label to find",
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
        context("by label regex") {
            val expectedCount = 3
            val predicates = fabricator.random<List<Predicate>>().toMutableList()
            (0 until 3).forEach {
                predicates[it] = predicates[it].copy(label = "label to find ($it)")
            }
            predicates.forEach(repository::save)

            val expected = predicates.take(expectedCount)
            val result = repository.findAllByLabelMatchesRegex(
                """^label to find \(\d\)$""",
                PageRequest.of(0, 5)
            )

            context("with pagination") {
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
        context("by label containing") {
            val expectedCount = 3
            val predicates = fabricator.random<List<Predicate>>().toMutableList()
            (0 until 3).forEach {
                predicates[it] = predicates[it].copy(label = "label to find")
            }
            predicates.forEach(repository::save)

            val expected = predicates.take(expectedCount)
            val result = repository.findAllByLabelContaining("to find", PageRequest.of(0, 5))

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

    context("deleting a predicate") {
        it("by predicate id removes it from the repository") {
            val expected = fabricator.random<Predicate>()
            repository.save(expected)
            repository.deleteByPredicateId(expected.id!!)
            repository.findByPredicateId(expected.id!!).isPresent shouldBe false
        }
    }

    it("delete all predicates") {
        repeat(3) {
            repository.save(createPredicate(id = PredicateId(it.toLong())))
        }
        // PredicateRepository has no count method
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 3
        repository.deleteAll()
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 0
    }

    context("requesting a new identity") {
        it("returns a valid id") {
            repository.nextIdentity() shouldNotBe null
        }
        it("returns an id that is not yet in the repository") {
            val predicate = createPredicate(id = repository.nextIdentity())
            repository.save(predicate)
            val id = repository.nextIdentity()
            repository.findByPredicateId(id).isPresent shouldBe false
        }
    }
}
