package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotMatch
import org.orkg.common.ContributorId
import org.orkg.common.ObservatoryId
import org.orkg.common.OrganizationId
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.BundleConfiguration
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Literals
import org.orkg.graph.domain.Predicate
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
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

fun <
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
> statementRepositoryContract(
    repository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P,
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
        )
            .withStandardMappings()
            .withGraphMappings()
    )

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

            val actual = repository.findByStatementId(expected.id).orElse(null)

            actual shouldNotBe null
            actual.asClue {
                it.id shouldBe expected.id
                it.subject shouldBe expected.subject
                it.predicate shouldBe expected.predicate
                it.`object` shouldBe expected.`object`
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
            val found = repository.findByStatementId(original.id).get()
            found shouldBe original

            val modifiedSubject = createResource(ThingId("R3"))
            val modified = found.copy(subject = modifiedSubject)
            saveStatement(modified)

            repository.findAll(PageRequest.of(0, Int.MAX_VALUE)).toSet().size shouldBe 1
            repository.findByStatementId(original.id).get().subject shouldBe modifiedSubject
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
                1 to 2,
                2 to 3,
                2 to 4,
                4 to 7,
                7 to 3,
                1 to 5,
                6 to 1,
                8 to 9
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
                    val actual = repository.countStatementsInPaperSubgraph(ThingId("R1"))
                    actual shouldBe 6
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countStatementsInPaperSubgraph(ThingId("missing"))
                    actual shouldBe 0
                }
            }
            context("about a resource") {
                it("returns the correct result") {
                    graph.forEach(saveStatement)
                    repository.countIncomingStatementsById(ThingId("R1")) shouldBe 1
                    repository.countIncomingStatementsById(ThingId("R3")) shouldBe 2
                }
                it("returns zero when the resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countIncomingStatementsById(ThingId("missing"))
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
                    val actual = repository.countAllIncomingStatementsById(resourceIds)
                    actual shouldContainExactly expected
                }
                it("returns empty result when no ids are given") {
                    graph.forEach(saveStatement)
                    val actual = repository.countAllIncomingStatementsById(setOf())
                    actual.size shouldBe 0
                }
                // TODO: do we expect results for missing resource ids to be zero or missing?
                it("returns nothing when the given resource is missing in the graph") {
                    graph.forEach(saveStatement)
                    val actual = repository.countAllIncomingStatementsById(setOf(ThingId("missing")))
                    actual.size shouldBe 0
                }
            }
        }
    }

    context("finding several thing descriptions") {
        val hasDescription = fabricator.random<Predicate>().copy(id = Predicates.description)
        val graph = listOf(
            fabricator.random<GeneralStatement>().copy(
                subject = fabricator.random<Resource>(),
                predicate = hasDescription,
                `object` = fabricator.random<Literal>().copy(
                    label = "description 1",
                    datatype = Literals.XSD.STRING.prefixedUri
                )
            ),
            fabricator.random<GeneralStatement>().copy(
                subject = fabricator.random<Resource>(),
                predicate = hasDescription,
                `object` = fabricator.random<Literal>().copy(
                    label = "description 2",
                    datatype = Literals.XSD.STRING.prefixedUri
                )
            ),
            fabricator.random<GeneralStatement>().copy(
                subject = fabricator.random<Resource>(),
                predicate = hasDescription,
                `object` = fabricator.random<Resource>()
            )
        )

        it("returns the correct result") {
            graph.forEach(saveStatement)
            val result = repository.findAllDescriptionsById(graph.map { it.subject.id }.toSet())
            result shouldBe mapOf(
                graph[0].subject.id to "description 1",
                graph[1].subject.id to "description 2"
            )
        }
        it("returns empty result when no ids are given") {
            graph.forEach(saveStatement)
            val actual = repository.countAllIncomingStatementsById(setOf())
            actual.size shouldBe 0
        }
        it("returns nothing when the given resource is missing in the graph") {
            graph.forEach(saveStatement)
            val actual = repository.countAllIncomingStatementsById(setOf(ThingId("missing")))
            actual.size shouldBe 0
        }
    }

    context("deleting a statement") {
        it("by statement instance removes it from the repository") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.delete(expected)
            repository.findByStatementId(expected.id).isPresent shouldBe false
        }
        it("by statement id removes it from the repository") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.deleteByStatementId(expected.id)
            repository.findByStatementId(expected.id).isPresent shouldBe false
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
            repository.deleteByStatementIds(expected.map { it.id }.toSet())
            repository.findAll(PageRequest.of(0, 10)).totalElements shouldBe 0
        }
        it("by statement id removes them from the repository (singleton)") {
            val expected: GeneralStatement = fabricator.random()
            saveStatement(expected)
            repository.deleteByStatementIds(setOf(expected.id))
            repository.findByStatementId(expected.id).isPresent shouldBe false
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
                    expected.map { it.id }.toSet(),
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
                    expected.map { it.id }.toSet(),
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

    describe("finding a resource by DOI") {
        it("returns the correct result") {
            val hasDOI = createPredicate(id = Predicates.hasDOI)
            val doi = fabricator.random<String>()
            val resource1 = createResource(
                id = ThingId("R1"),
                classes = setOf(Classes.paper)
            )
            val resource2 = resource1.copy(
                id = ThingId("R2"),
                createdAt = resource1.createdAt.minusHours(1)
            )
            val resource3 = resource1.copy(
                id = ThingId("R3"),
                classes = setOf(ThingId(fabricator.random())),
                createdAt = resource1.createdAt.plusHours(1)
            )
            val resource1HasDoi = createStatement(
                id = fabricator.random(),
                subject = resource1,
                predicate = hasDOI,
                `object` = createLiteral(id = fabricator.random(), label = doi)
            )
            val resource2HasDoi = createStatement(
                id = fabricator.random(),
                subject = resource2,
                predicate = hasDOI,
                `object` = createLiteral(id = fabricator.random(), label = doi)
            )
            val resource3HasDoi = createStatement(
                id = fabricator.random(),
                subject = resource3,
                predicate = hasDOI,
                `object` = createLiteral(id = fabricator.random(), label = doi)
            )
            saveStatement(resource1HasDoi)
            saveStatement(resource2HasDoi)
            saveStatement(resource3HasDoi)

            val actual = repository.findByDOI(doi, setOf(Classes.paper, Classes.comparison))
            actual.isPresent shouldBe true
            actual.get() shouldBe resource1

            val upper = repository.findByDOI(doi.uppercase(), setOf(Classes.paper, Classes.comparison))
            upper.isPresent shouldBe true
            upper.get() shouldBe resource1

            val lower = repository.findByDOI(doi.lowercase(), setOf(Classes.paper, Classes.comparison))
            lower.isPresent shouldBe true
            lower.get() shouldBe resource1
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

            val result = repository.findAllProblemsByObservatoryId(observatoryId, PageRequest.of(0, 5))

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

            val expected = setOf(
                ResourceContributor(resource.createdBy, resource.createdAt),
                ResourceContributor(otherResource.createdBy, otherResource.createdAt),
                ResourceContributor(resourceRelatesToOtherResource.createdBy, resourceRelatesToOtherResource.createdAt!!),
                ResourceContributor(anotherResource.createdBy, anotherResource.createdAt),
                ResourceContributor(otherResourceRelatesToAnotherResource.createdBy, otherResourceRelatesToAnotherResource.createdAt!!)
            ).map {
                it.copy(createdAt = it.createdAt.withSecond(0).withNano(0).withOffsetSameInstant(ZoneOffset.UTC))
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
                result.content.map { it.createdAt }
                    .zipWithNext { a, b -> a shouldBeGreaterThan b }
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
}
