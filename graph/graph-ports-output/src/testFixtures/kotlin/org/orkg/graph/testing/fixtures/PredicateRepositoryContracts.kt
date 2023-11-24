package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import org.orkg.common.ThingId
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    R : PredicateRepository,
    S: StatementRepository,
    L: LiteralRepository
> predicateRepositoryContract(
    repository: R,
    statementRepository: S,
    literalRepository: L
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        repository.deleteAll()
        literalRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    describe("saving a predicate") {
        it("saves and loads all properties correctly") {
            val expected: Predicate = fabricator.random<Predicate>().copy(
                description = "some predicate description"
            )
            repository.save(expected)

            val descriptionStatement = createStatement(
                subject = expected,
                predicate = createPredicate(id = Predicates.description),
                `object` = createLiteral(label = expected.description!!)
            )
            repository.save(descriptionStatement.predicate)
            literalRepository.save(descriptionStatement.`object` as Literal)
            statementRepository.save(descriptionStatement)

            val actual = repository.findById(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it.id shouldBe expected.id
                it.description shouldBe it.description
            }
        }
        it("updates an already existing predicate") {
            val original: Predicate = fabricator.random()
            repository.save(original)
            val found = repository.findById(original.id).get()
            val modified = found.copy(label = "some new label, never seen before")
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findById(original.id).get().label shouldBe "some new label, never seen before"
        }
    }

    describe("finding several predicates") {
        context("by label") {
            val expectedCount = 3
            val label = "label-to-find"
            val predicates = fabricator.random<List<Predicate>>().toMutableList()
            (0 until 3).forEach {
                predicates[it] = predicates[it].copy(label = label)
            }

            val expected = predicates.take(expectedCount)

            context("with exact matching") {
                predicates.forEach(repository::save)
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
                predicates.forEach(repository::save)
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
                val resources = fabricator.random<Predicate>(10)
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
                val resources = fabricator.random<MutableList<Predicate>>()
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
                val resources = fabricator.random<List<Predicate>>()
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

    context("deleting a predicate") {
        it("by predicate id removes it from the repository") {
            val expected = fabricator.random<Predicate>()
            repository.save(expected)
            repository.deleteById(expected.id)
            repository.findById(expected.id).isPresent shouldBe false
        }
    }

    it("delete all predicates") {
        repeat(3) {
            repository.save(createPredicate(id = ThingId("P$it")))
        }
        // PredicateRepository has no count method
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 3
        repository.deleteAll()
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 0
    }

    context("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank")  {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
            it("that is prefixed with 'P'") {
                repository.nextIdentity().value[0] shouldBe 'P'
            }
        }
        it("returns an id that is not yet in the repository") {
            val predicate = createPredicate(id = repository.nextIdentity())
            repository.save(predicate)
            val id = repository.nextIdentity()
            repository.findById(id).isPresent shouldBe false
        }
    }
}
