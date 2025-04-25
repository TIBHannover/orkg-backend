package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.ThingRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.UUID

fun <
    T : ThingRepository,
    C : ClassRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    L : LiteralRepository,
> thingRepositoryContract(
    repository: T,
    classRepository: C,
    resourceRepository: R,
    predicateRepository: P,
    literalRepository: L,
) = describeSpec {
    beforeTest {
        classRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
        literalRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        )
            .withStandardMappings()
            .withGraphMappings()
    )

    fun save(thing: Thing) {
        when (thing) {
            is Class -> classRepository.save(thing)
            is Literal -> literalRepository.save(thing)
            is Predicate -> predicateRepository.save(thing)
            is Resource -> resourceRepository.save(thing)
        }
    }

    describe("finding several things") {
        context("with filters") {
            context("using no parameters") {
                val things = fabricator.random<List<Thing>>()
                things.forEach(::save)

                val expected = things.sortedBy { it.createdAt }.take(10)
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
                    result.totalElements shouldBe things.size
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
                val things = fabricator.random<List<Predicate>>().toMutableList()
                (0 until 3).forEach {
                    things[it] = things[it].copy(label = label)
                }

                val expected = things.take(expectedCount)

                context("with exact matching") {
                    things.forEach(::save)
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
                    things.forEach(::save)
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
            context("by visibility") {
                val things = fabricator.random<List<Resource>>().mapIndexed { index, thing ->
                    thing.copy(
                        visibility = Visibility.entries[index % Visibility.entries.size]
                    )
                }
                VisibilityFilter.entries.forEach { visibilityFilter ->
                    context("when visibility is $visibilityFilter") {
                        things.forEach(::save)
                        val expected = things.filter { it.visibility in visibilityFilter.targets }
                        val result = repository.findAll(
                            visibility = visibilityFilter,
                            pageable = PageRequest.of(0, 10)
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
                        it("sorts the results by creation date by default") {
                            result.content.zipWithNext { a, b ->
                                a.createdAt shouldBeLessThan b.createdAt
                            }
                        }
                    }
                }
            }
            context("by created by") {
                val expectedCount = 3
                val things = fabricator.random<List<Literal>>().toMutableList()
                val createdBy = ContributorId(UUID.randomUUID())
                (0 until 3).forEach {
                    things[it] = things[it].copy(createdBy = createdBy)
                }
                things.forEach(::save)

                val expected = things.take(expectedCount)
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
                val things = fabricator.random<List<Literal>>().mapIndexed { index, thing ->
                    thing.copy(
                        createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong())
                    )
                }
                things.forEach(::save)

                val expected = things.take(expectedCount)
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
                val things = fabricator.random<List<Resource>>().mapIndexed { index, thing ->
                    thing.copy(
                        createdAt = OffsetDateTime.now(fixedClock).plusHours(index.toLong())
                    )
                }
                things.forEach(::save)

                val expected = things.take(expectedCount)
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
            context("by including class") {
                val expectedCount = 3
                val things = fabricator.random<List<Resource>>().toMutableList()
                val `class` = ThingId("SomeClass")
                (0 until 3).forEach {
                    things[it] = things[it].copy(classes = things[it].classes + `class`)
                }
                things.forEach(::save)

                val expected = things.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    includeClasses = setOf(`class`)
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
            context("by excluding class") {
                val expectedCount = 3
                val `class` = ThingId("SomeClass")
                val things = fabricator.random<List<Resource>>().mapIndexed { index, thing ->
                    if (index < expectedCount) {
                        thing
                    } else {
                        thing.copy(classes = thing.classes + `class`)
                    }
                }
                things.forEach(::save)

                val expected = things.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    excludeClasses = setOf(`class`)
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
            context("by observatory id") {
                val expectedCount = 3
                val things = fabricator.random<List<Resource>>().toMutableList()
                val observatoryId = ObservatoryId(UUID.randomUUID())
                (0 until 3).forEach {
                    things[it] = things[it].copy(observatoryId = observatoryId)
                }
                things.forEach(::save)

                val expected = things.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    observatoryId = observatoryId
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
            context("by organization id") {
                val expectedCount = 3
                val things = fabricator.random<List<Resource>>().toMutableList()
                val organizationId = OrganizationId(UUID.randomUUID())
                (0 until 3).forEach {
                    things[it] = things[it].copy(organizationId = organizationId)
                }
                things.forEach(::save)

                val expected = things.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    organizationId = organizationId
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
                val things = fabricator.random<List<Class>>()
                things.forEach(::save)

                val expected = createResource(classes = setOf(Classes.paper))
                resourceRepository.save(expected)

                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    label = SearchString.of(expected.label, exactMatch = true),
                    visibility = VisibilityFilter.ALL_LISTED,
                    createdBy = expected.createdBy,
                    createdAtStart = expected.createdAt,
                    createdAtEnd = expected.createdAt,
                    includeClasses = expected.classes,
                    excludeClasses = setOf(ThingId("MissingClass")),
                    observatoryId = expected.observatoryId,
                    organizationId = expected.organizationId
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
                val things = fabricator.random<List<Resource>>().toMutableList()
                things[1] = things[1].copy(label = things[0].label)
                things.forEach(::save)

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
}
