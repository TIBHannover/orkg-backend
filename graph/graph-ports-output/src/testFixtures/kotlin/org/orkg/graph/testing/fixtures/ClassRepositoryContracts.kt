package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import org.eclipse.rdf4j.common.net.ParsedIRI
import org.orkg.common.ContributorId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.SearchString
import org.orkg.graph.output.ClassRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.util.UUID

fun <
    C : ClassRepository,
> classRepositoryContract(
    repository: C,
) = describeSpec {
    beforeTest {
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

    describe("saving a class") {
        it("saves and loads all properties correctly") {
            val expected: Class = fabricator.random<Class>()
            repository.save(expected)

            val actual = repository.findById(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.label shouldBe expected.label
                it.uri shouldBe expected.uri
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it.id shouldBe expected.id
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
        context("with filters") {
            context("using no parameters") {
                val classes = fabricator.random<List<Class>>()
                classes.forEach(repository::save)

                val expected = classes.sortedBy { it.createdAt }.take(10)
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
                    result.totalElements shouldBe classes.size
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
                val classes = fabricator.random<List<Class>>().toMutableList()
                (0 until 3).forEach {
                    classes[it] = classes[it].copy(label = label)
                }

                val expected = classes.take(expectedCount)

                context("with exact matching") {
                    classes.forEach(repository::save)
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
                    classes.forEach(repository::save)
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
                val classes = fabricator.random<List<Class>>().toMutableList()
                val createdBy = ContributorId(UUID.randomUUID())
                (0 until 3).forEach {
                    classes[it] = classes[it].copy(createdBy = createdBy)
                }
                classes.forEach(repository::save)

                val expected = classes.take(expectedCount)
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
                val classes = fabricator.random<List<Class>>().mapIndexed { index, `class` ->
                    `class`.copy(
                        createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong())
                    )
                }
                classes.forEach(repository::save)

                val expected = classes.take(expectedCount)
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
                val classes = fabricator.random<List<Class>>().mapIndexed { index, `class` ->
                    `class`.copy(
                        createdAt = OffsetDateTime.now(fixedClock).plusHours(index.toLong())
                    )
                }
                classes.forEach(repository::save)

                val expected = classes.take(expectedCount)
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
            context("by uri") {
                val classes = fabricator.random<List<Class>>()
                classes.forEach(repository::save)

                val expected = classes[3]
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    uri = expected.uri,
                )

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContain expected
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
            context("using all parameters") {
                val classes = fabricator.random<List<Class>>()
                classes.forEach(repository::save)

                val expected = createClass()
                repository.save(expected)

                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    label = SearchString.of(expected.label, exactMatch = true),
                    createdBy = expected.createdBy,
                    createdAtStart = expected.createdAt,
                    createdAtEnd = expected.createdAt,
                    uri = expected.uri,
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
                val classes = fabricator.random<List<Class>>().toMutableList()
                classes[1] = classes[1].copy(label = classes[0].label)
                classes.forEach(repository::save)

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
    }

    context("existence checks for multiple classes") {
        it("returns true when all classes exist") {
            val ids = (1..3).map { ThingId("$it") }.onEach { repository.save(createClass(id = it, uri = null)) }

            repository.existsAllById(ids.toSet()) shouldBe true
        }
        it("returns false when at least one class does not exist") {
            val ids = (1..3).map { ThingId("$it") }.onEach { repository.save(createClass(id = it, uri = null)) }
                .plus(listOf(ThingId("9")))

            repository.existsAllById(ids.toSet()) shouldBe false
        }
        it("returns false when the set of IDs is empty") {
            repository.existsAllById(emptySet()) shouldBe false
        }
    }

    describe("finding a class") {
        context("by uri") {
            val expected = fabricator.random<Class>().copy(
                uri = ParsedIRI.create("https://example.org/uri/to/find")
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
