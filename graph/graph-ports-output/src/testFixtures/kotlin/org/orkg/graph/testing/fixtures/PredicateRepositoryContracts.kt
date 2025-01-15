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
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    P : PredicateRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
> predicateRepositoryContract(
    repository: P,
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        repository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        )
            .withStandardMappings()
            .withGraphMappings()
    )

    val saveThing: (Thing) -> Unit = {
        when (it) {
            is Class -> classRepository.save(it)
            is Literal -> literalRepository.save(it)
            is Resource -> resourceRepository.save(it)
            is Predicate -> repository.save(it)
        }
    }

    val saveStatement: (GeneralStatement) -> Unit = {
        saveThing(it.subject)
        saveThing(it.predicate)
        saveThing(it.`object`)
        statementRepository.save(it)
    }

    describe("saving a predicate") {
        it("saves and loads all properties correctly") {
            val expected: Predicate = fabricator.random<Predicate>()
            repository.save(expected)

            val actual = repository.findById(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it.id shouldBe expected.id
                it.modifiable shouldBe expected.modifiable
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
        context("with filters") {
            context("using no parameters") {
                val predicates = fabricator.random<List<Predicate>>()
                predicates.forEach(repository::save)

                val expected = predicates.sortedBy { it.createdAt }.take(10)
                val pageable = PageRequest.of(0, 10)
                val result = repository.findAll(pageable)

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 10
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 2
                    result.totalElements shouldBe predicates.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
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
                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 5),
                        label = SearchString.of(label, exactMatch = true),
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
                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 5),
                        label = SearchString.of("label find", exactMatch = false)
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
            context("by created by") {
                val expectedCount = 3
                val predicates = fabricator.random<List<Predicate>>().toMutableList()
                val createdBy = ContributorId(UUID.randomUUID())
                (0 until 3).forEach {
                    predicates[it] = predicates[it].copy(createdBy = createdBy)
                }
                predicates.forEach(repository::save)

                val expected = predicates.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    createdBy = createdBy
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
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("by created at start") {
                val expectedCount = 3
                val predicates = fabricator.random<List<Predicate>>().mapIndexed { index, predicate ->
                    predicate.copy(
                        createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong())
                    )
                }
                predicates.forEach(repository::save)

                val expected = predicates.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    createdAtStart = expected.last().createdAt
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
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("by created at end") {
                val expectedCount = 3
                val predicates = fabricator.random<List<Predicate>>().mapIndexed { index, predicate ->
                    predicate.copy(
                        createdAt = OffsetDateTime.now(fixedClock).plusHours(index.toLong())
                    )
                }
                predicates.forEach(repository::save)

                val expected = predicates.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    createdAtEnd = expected.last().createdAt
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
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("using all parameters") {
                val predicates = fabricator.random<List<Predicate>>()
                predicates.forEach(repository::save)

                val expected = createPredicate()
                repository.save(expected)

                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    label = SearchString.of(expected.label, exactMatch = true),
                    createdBy = expected.createdBy,
                    createdAtStart = expected.createdAt,
                    createdAtEnd = expected.createdAt,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll setOf(expected)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe 1
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            it("sorts the results by multiple properties") {
                val predicates = fabricator.random<List<Predicate>>().toMutableList()
                predicates[1] = predicates[1].copy(label = predicates[0].label)
                predicates.forEach(repository::save)

                val sort = Sort.by("label").ascending().and(Sort.by("created_at").descending())
                val result = repository.findAll(PageRequest.of(0, 12, sort))

                result.content.zipWithNext { a, b ->
                    if (a.label == b.label) {
                        a.createdAt shouldBeGreaterThan b.createdAt
                    } else {
                        a.label shouldBeLessThan b.label
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
            it("that is not blank") {
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

    context("checking whether a predicate is used") {
        context("when no statements exist") {
            it("returns the correct result") {
                val actual = repository.isInUse(ThingId("Missing"))
                actual shouldBe false
            }
        }
        context("when used in a statement") {
            context("as a predicate") {
                it("returns the correct result") {
                    val statement = fabricator.random<GeneralStatement>()
                    saveStatement(statement)

                    val actual = repository.isInUse(statement.predicate.id)
                    actual shouldBe true
                }
            }
            context("as a subject") {
                it("returns the correct result") {
                    val subject = fabricator.random<Predicate>()
                    val statement = fabricator.random<GeneralStatement>().copy(
                        subject = subject
                    )
                    saveStatement(statement)

                    val actual = repository.isInUse(subject.id)
                    actual shouldBe false
                }
            }
            context("as an object") {
                it("returns the correct result") {
                    val `object` = fabricator.random<Predicate>()
                    val statement = fabricator.random<GeneralStatement>().copy(
                        `object` = `object`
                    )
                    saveStatement(statement)

                    val actual = repository.isInUse(`object`.id)
                    actual shouldBe true
                }
            }
        }
    }
}
