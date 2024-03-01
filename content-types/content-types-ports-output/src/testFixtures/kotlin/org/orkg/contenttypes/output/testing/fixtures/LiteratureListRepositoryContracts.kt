package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.OffsetDateTime
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.contenttypes.output.LiteratureListRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.SearchString
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.orkg.testing.fixedClock
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    LL : LiteratureListRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> literatureListRepositoryContract(
    repository: LL,
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
    }

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull // FIXME: because "id" is nullable
        ).withStandardMappings()
    ).withCustomMappings()

    val saveThing: (Thing) -> Unit = {
        when (it) {
            is Class -> classRepository.save(it)
            is Literal -> literalRepository.save(it)
            is Resource -> resourceRepository.save(it)
            is Predicate -> predicateRepository.save(it)
        }
    }

    val saveStatement: (GeneralStatement) -> Unit = {
        saveThing(it.subject)
        saveThing(it.predicate)
        saveThing(it.`object`)
        statementRepository.save(it)
    }

    data class TestGraph(
        val resources: List<Resource>,
        val statements: List<GeneralStatement>,
        val ignored: Set<Resource>
    ) {
        val expected: List<Resource> get() = (resources - ignored)

        fun save(): TestGraph {
            resources.forEach(resourceRepository::save)
            statements.forEach(saveStatement)
            return this
        }
    }

    // (unpublished 1)
    // (unpublished 2)
    // (unpublished 3) -> (published 1)
    // (unpublished 4) -> (published 2)
    // (unpublished 5) -> (published 3 + 4)
    // (unpublished 6) -> (published 4 + 6)
    fun createTestGraph(transform: (Int, Resource) -> Resource = { _, it -> it.copy(visibility = Visibility.DEFAULT) }): TestGraph {
        val resources = fabricator.random<List<Resource>>().mapIndexed(transform)
        val unpublished = resources.take(6).map { it.copy(classes = setOf(Classes.literatureList)) }
        val published = resources.drop(6).mapIndexed { index, it ->
            it.copy(
                classes = setOf(Classes.literatureListPublished),
                createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong())
            )
        }
        val statements = mutableListOf<GeneralStatement>()
        val hasPublishedVersion = fabricator.random<Predicate>().copy(id = Predicates.hasPublishedVersion)
        val ignored = mutableSetOf<Resource>()
        // link a single published list to an unpublished list (2x)
        for (i in 0..1) {
            statements.add(
                fabricator.random<GeneralStatement>().copy(
                    subject = unpublished[2 + i],
                    predicate = hasPublishedVersion,
                    `object` = published[i]
                )
            )
        }
        // link two published lists to an unpublished list (2x)
        for (i in 0..1) {
            for (j in 0..1) {
                statements.add(
                    fabricator.random<GeneralStatement>().copy(
                        subject = unpublished[4 + i],
                        predicate = hasPublishedVersion,
                        `object` = published[2 + i * 2 + j]
                    )
                )
                if (j > 0) {
                    // published, but outdated, so we want to ignore them later
                    ignored.add(published[2 + i * 2 + j])
                }
            }
        }
        return TestGraph(unpublished + published, statements, ignored)
    }

    describe("finding several literature lists") {
        context("with filters") {
            context("using no parameters") {
                val graph = createTestGraph().save()
                val expected = graph.expected.sortedBy { it.createdAt }.take(10)
                val pageable = PageRequest.of(0, 10)
                val result = repository.findAll(pageable)

                expected.size shouldNotBe 0

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 10
                    result.content shouldContainAll expected
                }
                it("pages the result correctly") {
                    result.size shouldBe 10
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe graph.expected.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
            context("by label") {
                val label = "label-to-find"
                val graph = createTestGraph { index, resource ->
                    resource.copy(label = if (index % 2 == 0) label else resource.label)
                }
                val expected = graph.expected.filter { it.label == label }
                expected.size shouldNotBe 0

                context("with exact matching") {
                    graph.save()
                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 10),
                        label = SearchString.of(label, exactMatch = true),
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
                context("with fuzzy matching") {
                    graph.save()
                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 10),
                        label = SearchString.of("label find", exactMatch = false)
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
            }
            context("by visibility") {
                val graph = createTestGraph { index, resource ->
                    resource.copy(visibility = Visibility.entries[index % Visibility.entries.size])
                }
                VisibilityFilter.entries.forEach { visibilityFilter ->
                    context("when visibility is $visibilityFilter") {
                        graph.save()
                        val expected = graph.expected.filter { it.visibility in visibilityFilter.targets }
                        val result = repository.findAll(
                            visibility = visibilityFilter,
                            pageable = PageRequest.of(0, 10)
                        )

                        expected.size shouldNotBe 0

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
                val createdBy = ContributorId(UUID.randomUUID())
                val graph = createTestGraph { index, resource ->
                    resource.copy(createdBy = if (index % 2 == 0) createdBy else resource.createdBy)
                }.save()

                val expected = graph.expected.filter { it.createdBy == createdBy }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    createdBy = createdBy
                )

                expected.size shouldNotBe 0

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
            context("by created at start") {
                val graph = createTestGraph { index, resource ->
                    resource.copy(createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong()))
                }.save()

                val createdAtStart = graph.expected[graph.expected.size / 2].createdAt
                val expected = graph.expected.filter { it.createdAt >= createdAtStart }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    createdAtStart = createdAtStart
                )

                expected.size shouldNotBe 0

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
                        a.createdAt shouldBeLessThanOrEqualTo b.createdAt
                    }
                }
            }
            context("by created at end") {
                val graph = createTestGraph { index, resource ->
                    resource.copy(createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong()))
                }.save()

                val createdAtEnd = graph.expected[graph.expected.size / 2].createdAt
                val expected = graph.expected.filter { it.createdAt <= createdAtEnd }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    createdAtEnd = createdAtEnd
                )

                expected.size shouldNotBe 0

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
                        a.createdAt shouldBeLessThanOrEqualTo b.createdAt
                    }
                }
            }
            context("by observatory id") {
                val observatoryId = ObservatoryId(UUID.randomUUID())
                val graph = createTestGraph { index, resource ->
                    resource.copy(observatoryId = if (index % 2 == 0) observatoryId else resource.observatoryId)
                }.save()

                val expected = graph.expected.filter { it.observatoryId == observatoryId }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    observatoryId = observatoryId
                )

                expected.size shouldNotBe 0

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
            context("by organization id") {
                val organizationId = OrganizationId(UUID.randomUUID())
                val graph = createTestGraph { index, resource ->
                    resource.copy(organizationId = if (index % 2 == 0) organizationId else resource.organizationId)
                }.save()

                val expected = graph.expected.filter { it.organizationId == organizationId }
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 10),
                    organizationId = organizationId
                )

                expected.size shouldNotBe 0

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
            context("by publication status") {
                val graph = createTestGraph().save()

                context("when published") {
                    graph.save()
                    val expected = graph.expected.filter { Classes.literatureListPublished in it.classes }

                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 5),
                        published = true
                    )

                    expected.size shouldNotBe 0

                    it("returns the correct result") {
                        result shouldNotBe null
                        result.content shouldNotBe null
                        result.content.size shouldBe expected.size
                        result.content shouldContainAll expected
                    }
                    it("pages the result correctly") {
                        result.size shouldBe 5
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
                context("when unpublished") {
                    graph.save()
                    val expected = graph.expected.filter { Classes.literatureList in it.classes }

                    val result = repository.findAll(
                        pageable = PageRequest.of(0, 10),
                        published = false
                    )

                    expected.size shouldNotBe 0

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
            context("using all parameters") {
                val graph = createTestGraph().save()
                val expected = graph.resources.first()

                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    label = SearchString.of(expected.label, exactMatch = true),
                    visibility = VisibilityFilter.ALL_LISTED,
                    createdBy = expected.createdBy,
                    createdAtStart = expected.createdAt,
                    createdAtEnd = expected.createdAt,
                    observatoryId = expected.observatoryId,
                    organizationId = expected.organizationId,
                    published = false
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
                createTestGraph { index, resource ->
                  if (index < 2) {
                      resource.copy(label = "label")
                  } else {
                      resource
                  }
                }.save()
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
