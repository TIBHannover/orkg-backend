package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.time.format.DateTimeFormatter.ofPattern
import java.util.*
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.PredicateUsageCount
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.ResourceContributor
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchFilter.Operator
import org.orkg.graph.domain.SearchFilter.Value
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.OwnershipInfo
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.testing.fixedClock
import org.orkg.testing.pageOf
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

fun <
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> statementRepositoryContract(
    repository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P
) = describeSpec {
    beforeTest {
        repository.deleteAll()
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
        repository.save(it)
    }

    describe("saving a statement") {
        it("saves and loads all properties correctly") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)

            val actual = repository.findByStatementId(expected.id!!).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.subject shouldBe expected.subject // FIXME: deep check
                it.predicate shouldBe expected.predicate // FIXME: deep check
                it.`object` shouldBe expected.`object` // FIXME: deep check
                it.createdAt shouldBe expected.createdAt
                it.createdBy shouldBe expected.createdBy
                it.index shouldBe expected.index
            }
        }
        // Disabled because the expected functionality is not supported by Spring Data Neo4j
        xit("updates an already existing statement") {
            val original = createStatement(
                subject = createResource(ThingId("R1")),
                `object` = createResource(ThingId("R2"))
            )
            saveStatement(original)
            val found = repository.findByStatementId(original.id!!).get()
            found shouldBe original

            val modifiedSubject = createResource(ThingId("R3"))
            val modified = found.copy(subject = modifiedSubject)
            saveStatement(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findByStatementId(original.id!!).get().subject shouldBe modifiedSubject
        }
    }

    describe("counting statements") {
        context("returns the correct result when") {
            it("some statements exist") {
                (0L until 3L).forEach {
                    saveStatement(createStatement(id = StatementId(it)))
                }
                repository.count() shouldBe 3
            }
            it("no statements exist") {
                repository.count() shouldBe 0
            }
        }
        context("in a graph") {
            //     6   4 → 7  8 → 9
            //     ↓   ↑   ↓
            // 5 ← 1 → 2 → 3
            val resources = mutableMapOf<Int, Resource>()
            val resourceFactory = { id: Int ->
                { createResource(id = ThingId("R$id")) }
            }
            val graph = listOf(
                1 to 2, 2 to 3, 2 to 4, 4 to 7,
                7 to 3, 1 to 5, 6 to 1, 8 to 9
            ).map {
                val subject = resources.getOrPut(it.first, resourceFactory(it.first))
                val `object` = resources.getOrPut(it.second, resourceFactory(it.second))
                val statement = createStatement(
                    id = fabricator.random(),
                    subject = subject,
                    `object` = `object`
                )
                statement
            }
            context("recursively by resource id") {
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    val actual = repository.countByIdRecursive(ThingId("R1"))
                    actual shouldBe 6
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countByIdRecursive(ThingId("missing"))
                    actual shouldBe 0
                }
            }
            context("about a resource") {
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    repository.countStatementsAboutResource(ThingId("R1")) shouldBe 1
                    repository.countStatementsAboutResource(ThingId("R3")) shouldBe 2
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsAboutResource(ThingId("missing"))
                    actual shouldBe 0
                }
            }
            context("about several resources") {
                // TODO: do we expect results for missing resource ids to be zero or missing?
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    val expected = mapOf(
                        1L to 1L,
                        3L to 2L,
                        // 10L to 0L
                    ).mapKeys { ThingId("R${it.key}") }
                    val resourceIds = expected.keys + ThingId("R10")
                    val actual = repository.countStatementsAboutResources(resourceIds)
                    actual shouldContainExactly expected
                }
                it("returns empty result when no ids are given") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsAboutResources(setOf())
                    actual.size shouldBe 0
                }
                // TODO: do we expect results for missing resource ids to be zero or missing?
                it("returns nothing when the given resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsAboutResources(setOf(ThingId("missing")))
                    actual.size shouldBe 0
                }
            }
        }
    }

    context("deleting a statement") {
        it("by statement instance removes it from the repository") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.delete(expected)
            repository.findByStatementId(expected.id!!).isPresent shouldBe false
        }
        it("by statement id removes it from the repository") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.deleteByStatementId(expected.id!!)
            repository.findByStatementId(expected.id!!).isPresent shouldBe false
        }
        it("does not throw if statement does not exist") {
            val id = StatementId("S123456789")
            repository.findByStatementId(id).isPresent shouldBe false

            repository.deleteByStatementId(id)
        }
    }

    context("deleting several statements") {
        it("by statement id removes them from the repository") {
            val expected: List<GeneralStatement> = fabricator.random()
            expected.forEach(saveStatement)
            repository.findAll(PageRequest.of(0, 10)).totalElements shouldBe expected.size
            repository.deleteByStatementIds(expected.map { it.id!! }.toSet())
            repository.findAll(PageRequest.of(0, 10)).totalElements shouldBe 0
        }
        it("by statement id removes them from the repository (singleton)") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.deleteByStatementIds(setOf(expected.id!!))
            repository.findByStatementId(expected.id!!).isPresent shouldBe false
        }
    }

    it("delete all statements") {
        (0L until 3L).forEach {
            saveStatement(createStatement(id = StatementId(it)))
        }
        repository.count() shouldBe 3
        repository.deleteAll()
        repository.count() shouldBe 0
    }

    describe("finding several statements") {
        context("with filters") {
            context("using no parameters") {
                val statements = fabricator.random<List<GeneralStatement>>()
                statements.forEach(saveStatement)

                val expected = statements.sortedBy { it.createdAt }.take(10)
                val pageable = PageRequest.of(0, 10)
                val result = repository.findAll(pageable = pageable)

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
                    result.totalElements shouldBe statements.size
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by subject classes") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val subjectClasses = setOf(Classes.paper, Classes.comparison)
                (0 until 3).forEach {
                    val subject = fabricator.random<Resource>().let { resource ->
                        resource.copy(classes = resource.classes + subjectClasses)
                    }
                    statements[it] = statements[it].copy(subject = subject)
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    subjectClasses = subjectClasses
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by subject id") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val subject = fabricator.random<Resource>()
                (0 until 3).forEach {
                    statements[it] = statements[it].copy(subject = subject)
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    subjectId = subject.id
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by subject label") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val label = "label to find"
                (0 until 3).forEach {
                    statements[it] = statements[it].copy(subject = fabricator.random<Resource>().copy(label = label))
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    subjectLabel = label
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by predicate id") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val predicate = createPredicate(ThingId("UniquePredicateId"))
                (0 until 3).forEach {
                    statements[it] = statements[it].copy(predicate = predicate)
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    predicateId = predicate.id
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by created by") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val createdBy = ContributorId(UUID.randomUUID())
                (0 until 3).forEach {
                    statements[it] = statements[it].copy(createdBy = createdBy)
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by created at start") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().mapIndexed { index, statement ->
                    statement.copy(
                        createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong())
                    )
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by created at end") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().mapIndexed { index, statement ->
                    statement.copy(
                        createdAt = OffsetDateTime.now(fixedClock).plusHours(index.toLong())
                    )
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by object classes") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val objectClasses = setOf(Classes.paper, Classes.comparison)
                (0 until 3).forEach {
                    val `object` = fabricator.random<Resource>().let { resource ->
                        resource.copy(classes = resource.classes + objectClasses)
                    }
                    statements[it] = statements[it].copy(`object` = `object`)
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    objectClasses = objectClasses
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by object id") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val `object` = fabricator.random<Resource>()
                (0 until 3).forEach {
                    statements[it] = statements[it].copy(`object` = `object`)
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    objectId = `object`.id
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("by object label") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                val label = "label to find"
                (0 until 3).forEach {
                    statements[it] = statements[it].copy(`object` = fabricator.random<Resource>().copy(label = label))
                }
                statements.forEach(saveStatement)

                val expected = statements.take(expectedCount)
                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    objectLabel = label
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("using all parameters") {
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                statements.forEach(saveStatement)

                val expected = fabricator.random<GeneralStatement>().copy(
                    subject = fabricator.random<Resource>(),
                    `object` = fabricator.random<Resource>()
                )
                saveStatement(expected)

                val result = repository.findAll(
                    pageable = PageRequest.of(0, 5),
                    subjectClasses = (expected.subject as Resource).classes,
                    subjectId = expected.subject.id,
                    subjectLabel = expected.subject.label,
                    predicateId = expected.predicate.id,
                    createdBy = expected.createdBy,
                    createdAtStart = expected.createdAt,
                    createdAtEnd = expected.createdAt,
                    objectClasses = (expected.`object` as Resource).classes,
                    objectId = expected.`object`.id,
                    objectLabel = expected.`object`.label
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            it("sorts the results by multiple properties") {
                val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
                statements[1] = statements[1].copy(subject = statements[0].subject)
                statements.forEach(repository::save)

                val sort = Sort.by("sub.label").ascending().and(Sort.by("created_at").descending())
                val result = repository.findAll(PageRequest.of(0, 12, sort))

                result.content.zipWithNext { a, b ->
                    if (a.subject.label == b.subject.label) {
                        a.createdAt!! shouldBeGreaterThan b.createdAt!!
                    } else {
                        a.subject.label shouldBeLessThan b.subject.label
                    }
                }
            }
        }
        context("by id") {
            context("with a single id") {
                val expectedCount = 1
                val statements = fabricator.random<List<GeneralStatement>>()
                val expected = statements.take(expectedCount)

                statements.forEach(saveStatement)

                val result = repository.findAllByStatementIdIn(
                    expected.map { it.id!! }.toSet(),
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
            context("with multiple ids") {
                val expectedCount = 3
                val statements = fabricator.random<List<GeneralStatement>>()
                val expected = statements.take(expectedCount)

                statements.forEach(saveStatement)

                val result = repository.findAllByStatementIdIn(
                    expected.map { it.id!! }.toSet(),
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
                        a.createdAt!! shouldBeLessThan b.createdAt!!
                    }
                }
            }
        }
        context("by subject ids") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val ids = (0 until expectedCount).map {
                // we generate the following mapping:
                // index(0) -> id(0)
                // index(1) -> id(1)
                // index(2) -> id(1)
                val id = it.coerceAtMost(1).toLong()
                val subject = createResource(
                    id = ThingId("R$id"),
                    // We need to fix the time here, to make equality work.
                    createdAt = OffsetDateTime.parse("2023-01-24T16:09:18.557233+01:00")
                )
                statements[it] = statements[it].copy(
                    subject = subject
                )
                subject.id
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllBySubjects(ids, PageRequest.of(0, 5))

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
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("by object ids") {
            val expectedCount = 3
            val statements = fabricator.random<List<GeneralStatement>>().toMutableList()
            val ids = (0 until 2).map {
                val id = ThingId("R$it")
                val `object` = createResource(id = id)
                statements[it] = statements[it].copy(`object` = `object`)
                if (it == 1)
                    statements[it + 1] = statements[it + 1].copy(`object` = `object`)
                id
            }
            statements.forEach(saveStatement)
            val expected = statements.take(expectedCount)

            val result = repository.findAllByObjects(ids, PageRequest.of(0, 5))

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
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        context("as a bundle") {
            context("with a minimum level of hops") {
                val statement1 = fabricator.random<GeneralStatement>()
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`
                )
                val statement3 = fabricator.random<GeneralStatement>().copy(
                    subject = statement2.`object`
                )
                saveStatement(statement1)
                saveStatement(statement2)
                saveStatement(statement3)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = 1,
                        maxLevel = null,
                        blacklist = emptyList(),
                        whitelist = emptyList()
                    ),
                    Sort.unsorted()
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 2
                    result shouldContainAll setOf(statement2, statement3)
                }
                it("sorts the results by creation date by default") {
                    result.zipWithNext { a, b ->
                        a.createdAt!! shouldBeGreaterThan b.createdAt!!
                    }
                }
            }
            context("with a maximum level of hops") {
                val statement1 = fabricator.random<GeneralStatement>()
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`
                )
                val statement3 = fabricator.random<GeneralStatement>().copy(
                    subject = statement2.`object`
                )
                val statement4 = fabricator.random<GeneralStatement>().copy(
                    subject = statement3.`object`
                )
                saveStatement(statement1)
                saveStatement(statement2)
                saveStatement(statement3)
                saveStatement(statement4)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = null,
                        maxLevel = 2,
                        blacklist = emptyList(),
                        whitelist = emptyList()
                    ),
                    Sort.unsorted()
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 2
                    result shouldContainAll setOf(statement1, statement2)
                }
                it("sorts the results by creation date by default") {
                    result.zipWithNext { a, b ->
                        a.createdAt!! shouldBeGreaterThan b.createdAt!!
                    }
                }
            }
            context("with a blacklist for classes") {
                val statement1 = fabricator.random<GeneralStatement>()
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`
                )
                val statement3 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`,
                    `object` = fabricator.random<Resource>()
                )
                saveStatement(statement1)
                saveStatement(statement2)
                saveStatement(statement3)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = null,
                        maxLevel = null,
                        blacklist = (statement3.`object` as Resource).classes.take(2),
                        whitelist = emptyList()
                    ),
                    Sort.unsorted()
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 2
                    result shouldContainAll setOf(statement1, statement2)
                }
                it("sorts the results by creation date by default") {
                    result.zipWithNext { a, b ->
                        a.createdAt!! shouldBeGreaterThan b.createdAt!!
                    }
                }
            }
            context("with a whitelist for classes") {
                val statement1 = fabricator.random<GeneralStatement>().copy(
                    `object` = fabricator.random<Resource>()
                )
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`,
                    `object` = fabricator.random<Resource>().copy(classes = (statement1.`object` as Resource).classes)
                )
                val statement3 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`
                )
                saveStatement(statement1)
                saveStatement(statement2)
                saveStatement(statement3)
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = null,
                        maxLevel = null,
                        blacklist = emptyList(),
                        whitelist = (statement1.`object` as Resource).classes.take(2)
                    ),
                    Sort.unsorted()
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 2
                    result shouldContainAll setOf(statement1, statement2)
                }
                it("sorts the results by creation date by default") {
                    result.zipWithNext { a, b ->
                        a.createdAt!! shouldBeGreaterThan b.createdAt!!
                    }
                }
            }
            context("with a special sort order") {
                val createdBy1 = ContributorId("519fce4f-eee0-4841-9e71-6bdccb253ad3")
                val createdBy2 = ContributorId("6aaea2ec-394f-4fe9-ac78-4254d21f1181")
                val statement1 = fabricator.random<GeneralStatement>().copy(
                    `object` = fabricator.random<Resource>(),
                    createdBy = createdBy1
                )
                val statement2 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`,
                    createdBy = createdBy1
                )
                val statement3 = fabricator.random<GeneralStatement>().copy(
                    subject = statement1.`object`,
                    createdBy = createdBy2
                )
                saveStatement(statement1)
                saveStatement(statement2)
                saveStatement(statement3)

                val expected = listOf(statement1, statement2, statement3)
                    .sortedWith(Comparator.comparing<GeneralStatement, UUID> { it.createdBy.value }.reversed().thenBy { it.createdAt })
                val result = repository.fetchAsBundle(
                    statement1.subject.id,
                    BundleConfiguration(
                        minLevel = null,
                        maxLevel = null,
                        blacklist = emptyList(),
                        whitelist = emptyList()
                    ),
                    Sort.by("created_by").descending().and(Sort.by("created_at").ascending())
                )
                it("returns the correct result") {
                    result shouldNotBe null
                    result.count() shouldBe 3
                    result shouldContainInOrder expected
                }
            }
        }
    }

    describe("requesting a new identity") {
        context("returns a valid id") {
            it("that is not blank") {
                repository.nextIdentity().value shouldNotMatch """\s+"""
            }
            it("that is prefixed with 'S'") {
                repository.nextIdentity().value[0] shouldBe 'S'
            }
        }
        it("returns an id that is not yet in the repository") {
            val statement = createStatement(id = repository.nextIdentity())
            saveStatement(statement)
            val id = repository.nextIdentity()
            repository.findByStatementId(id).isPresent shouldBe false
        }
    }

    describe("finding a doi") {
        context("by contribution id") {
            val statements = mutableListOf<GeneralStatement>()
            val hasContribution = createPredicate(
                id = Predicates.hasContribution
            )
            val hasDOI = createPredicate(
                id = Predicates.hasDOI
            )
            repeat(2) {
                val paper = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.paper)
                )
                val contribution = createResource(
                    id = fabricator.random(),
                )
                val doi = createLiteral(
                    id = fabricator.random(),
                    label = fabricator.random()
                )
                val paperHasContribution = createStatement(
                    id = fabricator.random(),
                    subject = paper,
                    predicate = hasContribution,
                    `object` = contribution
                )
                val paperHasDoi = createStatement(
                    id = fabricator.random(),
                    subject = paper,
                    predicate = hasDOI,
                    `object` = doi
                )
                statements.add(paperHasContribution)
                statements.add(paperHasDoi)
            }

            it("returns the correct result") {
                val contribution = statements[0].`object` as Resource
                statements.forEach(saveStatement)

                val expected = statements[1].`object`
                val actual = repository.findDOIByContributionId(contribution.id)

                actual.isPresent shouldBe true
                actual.get() shouldBe expected
            }
        }
    }

    describe("finding several dois") {
        context("related to comparison") {
            val comparison = fabricator.random<Resource>()
                .copy(classes = setOf(Classes.comparison))
            val contributions = fabricator.random<List<Resource>>()
                .map { it.copy(classes = setOf(Classes.contribution)) }
            val papers = fabricator.random<List<Resource>>()
                .map { it.copy(classes = setOf(Classes.paper)) }

            val comparesContribution = fabricator.random<Predicate>().copy(id = Predicates.comparesContribution)
            contributions.forEach {
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparison,
                        predicate = comparesContribution,
                        `object` = it
                    )
                )
            }

            val hasDOI = fabricator.random<Predicate>().copy(id = Predicates.hasDOI)
            val dois = papers.take(5).map { paper ->
                val doi = fabricator.random<Literal>()
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = paper,
                        predicate = hasDOI,
                        `object` = doi
                    )
                )
                doi.label.trim()
            }
            saveStatement(
                fabricator.random<GeneralStatement>().copy(
                    subject = papers[5],
                    predicate = hasDOI,
                    `object` = fabricator.random<Literal>().copy(label = "")
                )
            )

            val hasContribution = fabricator.random<Predicate>().copy(id = Predicates.hasContribution)
            (papers zip contributions).forEach { (paper, contribution) ->
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = paper,
                        predicate = hasContribution,
                        `object` = contribution
                    )
                )
            }

            val result = repository.findAllDOIsRelatedToComparison(comparison.id)

            it("returns the correct result") {
                result shouldContainAll dois
                result.toList().size shouldBe dois.size
            }
        }
    }

    describe("finding a resource by DOI") {
        it("always returns the resource") {
            val hasDOI = createPredicate(id = Predicates.hasDOI)
            val doi = fabricator.random<String>()
            val resource = createResource(classes = setOf(ThingId(fabricator.random())))
            val resourceHasDoi = createStatement(
                subject = resource,
                predicate = hasDOI,
                `object` = createLiteral(label = doi)
            )
            saveStatement(resourceHasDoi)

            val actual = repository.findByDOI(doi)
            actual.isPresent shouldBe true
            actual.get() shouldBe resource

            val upper = repository.findByDOI(doi.uppercase())
            upper.isPresent shouldBe true
            upper.get() shouldBe resource

            val lower = repository.findByDOI(doi.lowercase())
            lower.isPresent shouldBe true
            lower.get() shouldBe resource
        }
    }

    describe("counting predicate usage") {
        context("for a single predicate") {
            context("when no statements exist") {
                it("returns the correct result") {
                    val actual = repository.countPredicateUsage(ThingId("Missing"))
                    actual shouldBe 0
                }
            }
            context("when used in a statement") {
                context("as a predicate") {
                    it("returns the correct result") {
                        val statement = fabricator.random<GeneralStatement>()
                        saveStatement(statement)

                        val actual = repository.countPredicateUsage(statement.predicate.id)
                        actual shouldBe 1
                    }
                }
                context("as a subject") {
                    it("returns the correct result") {
                        val subject = fabricator.random<Predicate>()
                        val statement = fabricator.random<GeneralStatement>().copy(
                            subject = subject
                        )
                        saveStatement(statement)
                        val description = fabricator.random<GeneralStatement>().copy(
                            subject = subject,
                            predicate = createPredicate(id = Predicates.description)
                        )
                        saveStatement(description)

                        val actual = repository.countPredicateUsage(subject.id)
                        actual shouldBe 1
                    }
                }
                context("as an object") {
                    it("returns the correct result") {
                        val `object` = fabricator.random<Predicate>()
                        val statement = fabricator.random<GeneralStatement>().copy(
                            `object` = `object`
                        )
                        saveStatement(statement)

                        val actual = repository.countPredicateUsage(`object`.id)
                        actual shouldBe 1
                    }
                }
            }
        }
        context("for all predicates") {
            context("when no statements exist") {
                val result = repository.countPredicateUsage(PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 0
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 0
                    result.totalElements shouldBe 0
                }
            }
            context("when several statements exist") {
                val statements = fabricator.random<MutableList<GeneralStatement>>()
                statements[1] = statements[1].copy(
                    predicate = statements[0].predicate
                )
                statements[2] = statements[2].copy(
                    predicate = statements[0].predicate
                )
                statements.forEach(saveStatement)

                val expected = statements.drop(3)
                    .map { PredicateUsageCount(it.predicate.id, 1) }
                    .plus(PredicateUsageCount(statements[0].predicate.id, 3))
                    .sortedWith(compareByDescending<PredicateUsageCount> { it.count }.thenBy { it.id })

                val result = repository.countPredicateUsage(PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 5
                    result.content shouldContainAll expected.take(5)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 2
                    result.totalElements shouldBe 10
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.count shouldBeGreaterThanOrEqual b.count
                    }
                }
            }
        }
    }

    describe("finding several papers") {
        val doi = fabricator.random<String>()
        val hasDoi = createPredicate(id = Predicates.hasDOI)
        context("by class and doi") {
            val doiLiteral = createLiteral(label = doi)

            val paper = createResource(
                id = fabricator.random(),
                classes = setOf(Classes.paper)
            )
            val paperHasDoi = createStatement(
                id = fabricator.random(),
                subject = paper,
                predicate = hasDoi,
                `object` = doiLiteral
            )

            val deletedPaper = createResource(
                id = fabricator.random(),
                classes = setOf(Classes.paperDeleted)
            )
            val deletedPaperHasDoi = createStatement(
                id = fabricator.random(),
                subject = deletedPaper,
                predicate = hasDoi,
                `object` = doiLiteral
            )

            context("with default case") {
                saveStatement(paperHasDoi)
                saveStatement(deletedPaperHasDoi)

                val result = repository.findAllBySubjectClassAndDOI(Classes.paper, doi, PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll setOf(paper)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe 1
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }

            context("with uppercase") {
                saveStatement(paperHasDoi)
                saveStatement(deletedPaperHasDoi)

                val result = repository.findAllBySubjectClassAndDOI(Classes.paper, doi.uppercase(), PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll setOf(paper)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe 1
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }

            context("with lowercase") {
                saveStatement(paperHasDoi)
                saveStatement(deletedPaperHasDoi)

                val result = repository.findAllBySubjectClassAndDOI(Classes.paper, doi.lowercase(), PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll setOf(paper)
                }
                it("pages the result correctly") {
                    result.size shouldBe 5
                    result.number shouldBe 0
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe 1
                }
                xit("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
        }
        context("by observatory id") {
            context("and visibility") {
                val observatoryId = ObservatoryId(UUID.randomUUID())
                val hasContribution = createPredicate(
                    id = Predicates.hasContribution
                )
                val paper1 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.paper),
                    observatoryId = observatoryId,
                    visibility = Visibility.FEATURED
                )
                val contribution1 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.contribution)
                )
                val paper1HasContribution1 = createStatement(
                    id = fabricator.random(),
                    subject = paper1,
                    predicate = hasContribution,
                    `object` = contribution1
                )

                saveStatement(paper1HasContribution1)

                val paper2 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.paper),
                    observatoryId = observatoryId
                )
                val paper2HasContribution1 = createStatement(
                    id = fabricator.random(),
                    subject = paper2,
                    predicate = hasContribution,
                    `object` = contribution1
                )

                saveStatement(paper2HasContribution1)

                val result = repository.findAllPapersByObservatoryIdAndFilters(observatoryId, emptyList(), VisibilityFilter.FEATURED, PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll setOf(paper1)
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
            context("and filter set") {
                val observatoryId = ObservatoryId(UUID.randomUUID())
                val hasContribution = createPredicate(
                    id = Predicates.hasContribution
                )
                val hasResearchProblem = createPredicate(
                    id = Predicates.hasResearchProblem
                )
                val hasKeyword = createPredicate(
                    id = ThingId("R394758")
                )

                val paper1 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.paper),
                    observatoryId = observatoryId,
                    visibility = Visibility.FEATURED
                )
                val contribution1 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.contribution)
                )
                val value1 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.problem)
                )
                val paper1HasContribution1 = createStatement(
                    id = fabricator.random(),
                    subject = paper1,
                    predicate = hasContribution,
                    `object` = contribution1
                )
                val contribution1HasProblemValue1 = createStatement(
                    id = fabricator.random(),
                    subject = contribution1,
                    predicate = hasResearchProblem,
                    `object` = value1
                )

                saveStatement(paper1HasContribution1)
                saveStatement(contribution1HasProblemValue1)

                val paper2 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.paper),
                    observatoryId = observatoryId,
                    visibility = Visibility.DEFAULT
                )
                val paper2HasContribution1 = createStatement(
                    id = fabricator.random(),
                    subject = paper2,
                    predicate = hasContribution,
                    `object` = contribution1
                )

                saveStatement(paper2HasContribution1)

                val paper3 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.paper),
                    observatoryId = observatoryId,
                    visibility = Visibility.FEATURED
                )
                val contribution2 = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.contribution)
                )
                val value2 = createLiteral(
                    id = fabricator.random()
                )
                val paper3HasContribution2 = createStatement(
                    id = fabricator.random(),
                    subject = paper3,
                    predicate = hasContribution,
                    `object` = contribution2
                )
                val contribution2HasKeywordValue2 = createStatement(
                    id = fabricator.random(),
                    subject = contribution2,
                    predicate = hasKeyword,
                    `object` = value2
                )

                saveStatement(paper3HasContribution2)
                saveStatement(contribution2HasKeywordValue2)

                val filterConfig = listOf(
                    SearchFilter(
                        path = listOf(Predicates.hasResearchProblem),
                        range = Classes.resources,
                        values = setOf(Value(Operator.EQ, value1.id.value)),
                        exact = false
                    )
                )

                val result = repository.findAllPapersByObservatoryIdAndFilters(observatoryId, filterConfig, VisibilityFilter.FEATURED, PageRequest.of(0, 5))

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe 1
                    result.content shouldContainAll setOf(paper1)
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
        }
    }

    describe("finding several research problems") {
        context("by observatory id") {
            val observatoryId = fabricator.random<ObservatoryId>()
            val expected = (0 until 2).map {
                val paper = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.paper),
                    observatoryId = observatoryId
                )
                val contribution = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.contribution)
                )
                val researchProblem = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.problem),
                    observatoryId = observatoryId
                )
                val paperHasContribution = createStatement(
                    id = fabricator.random(),
                    subject = paper,
                    predicate = createPredicate(Predicates.hasContribution), // hasContribution
                    `object` = contribution
                )
                val contributionHasResearchProblem = createStatement(
                    id = fabricator.random(),
                    subject = contribution,
                    predicate = createPredicate(Predicates.hasResearchProblem), // hasProblem
                    `object` = researchProblem
                )
                saveStatement(paperHasContribution)
                saveStatement(contributionHasResearchProblem)
                researchProblem
            }

            val result = repository.findProblemsByObservatoryId(observatoryId, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
        context("by organization id") {
            val organizationId = fabricator.random<OrganizationId>()
            val compareContribution = fabricator.random<Predicate>().copy(
                id = Predicates.comparesContribution
            )
            val hasResearchProblem = fabricator.random<Predicate>().copy(
                id = Predicates.hasResearchProblem
            )
            val expected = (0 until 2).map {
                val contribution = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.contribution)
                )
                val researchProblem = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.problem)
                )
                val comparison = createResource(
                    id = fabricator.random(),
                    classes = setOf(Classes.comparison),
                    organizationId = organizationId
                )
                val comparisonHasContribution = createStatement(
                    id = fabricator.random(),
                    subject = comparison,
                    predicate = compareContribution,
                    `object` = contribution
                )
                val contributionHasResearchProblem = createStatement(
                    id = fabricator.random(),
                    subject = contribution,
                    predicate = hasResearchProblem,
                    `object` = researchProblem
                )
                saveStatement(comparisonHasContribution)
                saveStatement(contributionHasResearchProblem)
                researchProblem
            }

            val result = repository.findAllProblemsByOrganizationId(organizationId, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 2
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 2
            }
            xit("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt shouldBeLessThan b.createdAt
                }
            }
        }
    }

    describe("finding a timeline") {
        context("by resource id") {
            val resource = fabricator.random<Resource>().copy(
                createdAt = OffsetDateTime.now(fixedClock)
            )

            setOf("ResearchField", "Problem", "Paper").forEach {
                val resourceForIt = fabricator.random<Resource>().copy(
                    classes = setOf(ThingId(it)),
                    createdAt = resource.createdAt.plusSeconds(145864)
                )
                val resourceRelatesToIt = fabricator.random<GeneralStatement>().copy(
                    subject = resource,
                    `object` = resourceForIt,
                    createdAt = resource.createdAt.plusSeconds(7897)
                )
                saveStatement(resourceRelatesToIt)
            }

            // Relate to some other Resource
            val otherResource = fabricator.random<Resource>().copy(
                createdAt = resource.createdAt.plusSeconds(5478)
            )
            val resourceRelatesToOtherResource = fabricator.random<GeneralStatement>().copy(
                subject = resource,
                `object` = otherResource,
                createdAt = resource.createdAt.plusSeconds(26158)
            )
            saveStatement(resourceRelatesToOtherResource)

            // Relate otherResource to another Resource
            val anotherResource = fabricator.random<Resource>().copy(
                createdAt = resource.createdAt.plusSeconds(9871)
            )
            val otherResourceRelatesToAnotherResource = fabricator.random<GeneralStatement>().copy(
                subject = otherResource,
                `object` = anotherResource,
                createdAt = resource.createdAt.plusSeconds(14659)
            )
            saveStatement(otherResourceRelatesToAnotherResource)

            // Relate to an old Resource
            val oldResource = fabricator.random<Resource>().copy(
                createdAt = resource.createdAt.minusSeconds(651456)
            )
            val resourceRelatesToOldResource = fabricator.random<GeneralStatement>().copy(
                subject = resource,
                `object` = oldResource,
                createdAt = resource.createdAt.minusSeconds(156168)
            )
            saveStatement(resourceRelatesToOldResource)

            val formatter = ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            val expected = setOf(
                resource.createdBy to resource.createdAt,
                otherResource.createdBy to otherResource.createdAt,
                resourceRelatesToOtherResource.createdBy to resourceRelatesToOtherResource.createdAt,
                anotherResource.createdBy to anotherResource.createdAt,
                otherResourceRelatesToAnotherResource.createdBy to otherResourceRelatesToAnotherResource.createdAt
            ).map {
                ResourceContributor(
                    it.first.toString(),
                    it.second!!
                        .withSecond(0)
                        .withNano(0)
                        .atZoneSameInstant(ZoneOffset.UTC)
                        .format(formatter)
                )
            }

            val result = repository.findTimelineByResourceId(resource.id, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 5
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 5
            }
            it("sorts the results by creation date by default") {
                result.content.map {
                    OffsetDateTime.parse(it.createdAt, ISO_OFFSET_DATE_TIME)
                }.zipWithNext { a, b ->
                    a shouldBeGreaterThan b
                }
            }
        }
    }

    describe("finding several contributors") {
        context("by resource id") {
            val resource = fabricator.random<Resource>()

            setOf("ResearchField", "Problem", "Paper").forEach {
                val resourceForIt = fabricator.random<Resource>().copy(
                    classes = setOf(ThingId(it))
                )
                val resourceRelatesToIt = fabricator.random<GeneralStatement>().copy(
                    subject = resource,
                    `object` = resourceForIt
                )
                saveStatement(resourceRelatesToIt)
            }

            // Relate to some other Resource
            val otherResource = fabricator.random<Resource>()
            val resourceRelatesToOtherResource = fabricator.random<GeneralStatement>().copy(
                subject = resource,
                `object` = otherResource
            )
            saveStatement(resourceRelatesToOtherResource)

            // Relate otherResource to another Resource
            val anotherResource = fabricator.random<Resource>()
            val otherResourceRelatesToAnotherResource = fabricator.random<GeneralStatement>().copy(
                subject = otherResource,
                `object` = anotherResource
            )
            saveStatement(otherResourceRelatesToAnotherResource)

            val expected = setOf(
                resource.createdBy,
                otherResource.createdBy,
                resourceRelatesToOtherResource.createdBy,
                anotherResource.createdBy,
                otherResourceRelatesToAnotherResource.createdBy
            )

            val result = repository.findAllContributorsByResourceId(resource.id, PageRequest.of(0, 5))

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe 5
                result.content shouldContainAll expected
            }
            it("pages the result correctly") {
                result.size shouldBe 5
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe 5
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.value.toString() shouldBeLessThan b.value.toString()
                }
            }
        }
    }

    describe("checking if a resource is used in a statement") {
        context("when no statements exist") {
            it("returns the correct result") {
                val resource = fabricator.random<Resource>()
                // Resource has to exist for neo4j repos
                resourceRepository.save(resource)
                repository.checkIfResourceHasStatements(resource.id) shouldBe false
            }
        }
        context("when a statement exists") {
            it("returns the correct result") {
                val statement = fabricator.random<GeneralStatement>().copy(
                    subject = fabricator.random<Resource>(),
                    `object` = fabricator.random<Resource>()
                )
                saveStatement(statement)
                repository.checkIfResourceHasStatements(statement.subject.id) shouldBe true
                repository.checkIfResourceHasStatements(statement.`object`.id) shouldBe true
            }
        }
    }

    describe("determining ownership") {
        context("when multiple ids are given") {
            it("returns the correct result") {
                val statements = fabricator.random<List<GeneralStatement>>()
                statements.forEach(saveStatement)
                val allStatementIds = statements.map { it.id!! }.toSet()
                val expected = statements.map { OwnershipInfo(it.id!!, it.createdBy) }.toSet()

                val actual = repository.determineOwnership(allStatementIds)

                actual shouldBe expected
            }
        }
        context("when one id is given") {
            it("returns the correct result") {
                val statement = fabricator.random<GeneralStatement>()
                saveStatement(statement)
                val expected = setOf(statement).map { OwnershipInfo(it.id!!, it.createdBy) }.toSet()

                val actual = repository.determineOwnership(setOf(statement.id!!))

                actual shouldBe expected
            }
        }
        context("when no id is given") {
            it("returns the correct result") {
                val expected = emptySet<OwnershipInfo>()

                val actual = repository.determineOwnership(emptySet())

                actual shouldBe expected
            }
        }
    }

    describe("finding several current comparisons") {
        context("by listed visibility") {
            context("without doi") {
                val comparisons = fabricator.random<List<Resource>>()
                    .map { it.copy(visibility = Visibility.UNLISTED, classes = setOf(Classes.comparison)) }
                    .toMutableList()
                comparisons[0] = comparisons[0].copy(visibility = Visibility.DEFAULT)
                comparisons[1] = comparisons[1].copy(visibility = Visibility.DEFAULT)
                comparisons[2] = comparisons[2].copy(visibility = Visibility.FEATURED)
                comparisons[3] = comparisons[3].copy(visibility = Visibility.FEATURED)
                comparisons[4] = comparisons[4].copy(visibility = Visibility.DEFAULT)
                comparisons[5] = comparisons[5].copy(visibility = Visibility.DEFAULT)
                comparisons[6] = comparisons[6].copy(visibility = Visibility.FEATURED)
                comparisons[7] = comparisons[7].copy(visibility = Visibility.FEATURED)

                // Workaround for in-memory repository, because it can only return comparisons that are used in at least one statement
                val description = fabricator.random<Predicate>().copy(id = Predicates.description)
                comparisons.forEach {
                    saveStatement(
                        fabricator.random<GeneralStatement>().copy(
                            subject = it,
                            predicate = description,
                            `object` = fabricator.random<Literal>()
                        )
                    )
                }

                val hasPreviousVersion = fabricator.random<Predicate>().copy(id = Predicates.hasPreviousVersion)
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparisons[0],
                        predicate = hasPreviousVersion,
                        `object` = comparisons[1]
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparisons[2],
                        predicate = hasPreviousVersion,
                        `object` = comparisons[3]
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparisons[4],
                        predicate = hasPreviousVersion,
                        `object` = comparisons[5]
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparisons[6],
                        predicate = hasPreviousVersion,
                        `object` = comparisons[7]
                    )
                )

                val hasDoi = fabricator.random<Predicate>().copy(id = Predicates.hasDOI)
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparisons[4],
                        predicate = hasDoi,
                        `object` = fabricator.random<Literal>()
                    )
                )
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = comparisons[6],
                        predicate = hasDoi,
                        `object` = fabricator.random<Literal>()
                    )
                )

                val pageable = PageRequest.of(0, 5)
                val expected = pageOf(comparisons[0], comparisons[2], pageable = PageRequest.of(0, 5))
                val result = repository.findAllCurrentListedAndUnpublishedComparisons(pageable)

                it("returns the correct result") {
                    result shouldNotBe null
                    result.content shouldNotBe null
                    result.content.size shouldBe expected.content.size
                    result.content shouldContainAll expected.content
                }
                it("pages the result correctly") {
                    result.size shouldBe expected.size
                    result.number shouldBe expected.number
                    result.totalPages shouldBe 1
                    result.totalElements shouldBe expected.totalElements
                }
                it("sorts the results by creation date by default") {
                    result.content.zipWithNext { a, b ->
                        a.createdAt shouldBeLessThan b.createdAt
                    }
                }
            }
        }
    }
}
