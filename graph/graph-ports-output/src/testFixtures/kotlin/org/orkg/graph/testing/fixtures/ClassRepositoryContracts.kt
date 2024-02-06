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
import java.net.URI
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    C : ClassRepository,
    S : StatementRepository,
    L : LiteralRepository,
    P : PredicateRepository
> classRepositoryContract(
    repository: C,
    statementRepository: S,
    literalRepository: L,
    predicateRepository: P
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        repository.deleteAll()
        literalRepository.deleteAll()
        predicateRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    describe("saving a class") {
        it("saves and loads all properties correctly") {
            val expected: Class = fabricator.random<Class>().copy(
                description = "some class description"
            )
            repository.save(expected)

            val descriptionStatement = createStatement(
                subject = expected,
                predicate = createPredicate(id = Predicates.description),
                `object` = createLiteral(label = expected.description!!),
            )
            predicateRepository.save(descriptionStatement.predicate)
            literalRepository.save(descriptionStatement.`object` as Literal)
            statementRepository.save(descriptionStatement)

            val actual = repository.findById(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.uri shouldBe expected.uri
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it.id shouldBe expected.id
                it.description shouldBe expected.description
                it.modifiable shouldBe expected.modifiable
            }
        }
        it("updates an already existing class") {
            val original: Class = fabricator.random()
            repository.save(original)
            val found = repository.findById(original.id).get()
            val modified = found.copy(label = "some new label, never seen before")
            repository.save(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findById(original.id).get().label shouldBe "some new label, never seen before"
        }
    }

    context("finding several classes") {
        context("without filters") {
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
        context("with filters") {
            context("using no parameters") {
                val resources = fabricator.random<Class>(10)
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
                val resources = fabricator.random<MutableList<Class>>()
                resources.forEach(repository::save)

                val expected = listOf(resources[0])
                val pageable = PageRequest.of(0, 10)
                val result = repository.findAllWithFilters(
                    uri = resources.first().uri.toString(),
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
                val resources = fabricator.random<List<Class>>()
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

    context("existence checks for multiple classes") {
        it("returns true when all classes exist") {
            val ids = (1..3).map { ThingId("$it") }.onEach { repository.save(createClass(id = it, uri = null)) }

            repository.existsAll(ids.toSet()) shouldBe true
        }
        it("returns false when at least one class does not exist") {
            val ids = (1..3).map { ThingId("$it") }.onEach { repository.save(createClass(id = it, uri = null)) }
                .plus(listOf(ThingId("9")))

            repository.existsAll(ids.toSet()) shouldBe false
        }
        it("returns false when the set of IDs is empty") {
            repository.existsAll(emptySet()) shouldBe false
        }
    }

    describe("finding several classes") {
        context("by class id") {
            val expectedCount = 3
            val classes = fabricator.random<List<Class>>()
            classes.forEach(repository::save)

            val expected = classes.take(expectedCount)
            val result = repository.findAllById(
                expected.map { it.id },
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
        context("by label") {
            val expectedCount = 3
            val label = "label-to-find"
            val classes = fabricator.random<List<Class>>().toMutableList()
            (0 until 3).forEach {
                classes[it] = classes[it].copy(label = label)
            }
            val expected = classes.take(expectedCount)

            context("with exact matching") {
                classes.forEach(repository::save)
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
                classes.forEach(repository::save)
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
    }

    describe("finding a class") {
        context("by uri") {
            val expected = fabricator.random<Class>().copy(
                uri = URI.create("https://example.org/uri/to/find")
            )
            val classes = fabricator.random<List<Class>>().plus(expected)

            it("returns the correct result") {
                classes.forEach(repository::save)
                val actual = repository.findByUri("https://example.org/uri/to/find")
                actual.isPresent shouldBe true
                actual.get() shouldBe expected
            }
            it("returns empty optional when not found") {
                classes.forEach(repository::save)
                val actual = repository.findByUri("https://example.org/not/found")
                actual.isPresent shouldBe false
            }
        }
    }

    it("delete all classes") {
        repeat(3) {
            repository.save(createClass(id = ThingId("$it"), uri = null))
        }
        // ClassRepository has no count method
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 3
        repository.deleteAll()
        repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).totalElements shouldBe 0
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank") {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
            it("that is prefixed with 'C'") {
                repository.nextIdentity().value[0] shouldBe 'C'
            }
        }
        it("returns an id that is not yet in the repository") {
            val `class` = createClass(id = repository.nextIdentity())
            repository.save(`class`)
            val id = repository.nextIdentity()
            repository.findById(id).isPresent shouldBe false
        }
    }
}
