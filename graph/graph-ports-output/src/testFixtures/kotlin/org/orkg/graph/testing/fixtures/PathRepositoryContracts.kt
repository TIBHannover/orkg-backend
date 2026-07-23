package org.orkg.graph.testing.fixtures

import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Path
import org.orkg.graph.domain.PathDirection
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PathRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.springframework.data.domain.PageRequest

fun <
    T : PathRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
> pathRepositoryContract(
    repository: T,
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P,
) = describeSpec {
    afterTest {
        statementRepository.deleteAll()
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
    }

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

    operator fun String.unaryPlus(): Pair<String, PathDirection> =
        this to PathDirection.OUTGOING

    operator fun String.unaryMinus(): Pair<String, PathDirection> =
        this to PathDirection.INCOMING

    // r4 ← r1 → r2
    // ↓     ↑   ↓
    // r5     r3
    val createTestGraph: () -> List<GeneralStatement> = createTestGraph@{
        val r1 = createResource(id = ThingId("R1"), classes = setOf(ThingId("C1")))
        val r2 = createResource(id = ThingId("R2"), classes = setOf(ThingId("C1")))
        val r3 = createResource(id = ThingId("R3"), classes = setOf(ThingId("C2")))
        val r4 = createResource(id = ThingId("R4"), classes = setOf(ThingId("C3")))
        val r5 = createResource(id = ThingId("R5"), classes = setOf(ThingId("C4")))
        listOf(
            createStatement(id = StatementId("S1"), subject = r1, `object` = r2),
            createStatement(id = StatementId("S2"), subject = r2, `object` = r3),
            createStatement(id = StatementId("S3"), subject = r3, `object` = r1),
            createStatement(id = StatementId("S4"), subject = r1, `object` = r4),
            createStatement(id = StatementId("S5"), subject = r4, `object` = r5),
        )
    }

    fun List<GeneralStatement>.pathOf(vararg statementIds: Pair<String, PathDirection>): Path {
        val idToStatement = associateBy { it.id }
        return statementIds.flatMap { (id, direction) ->
            val statementId = StatementId(id)
            val statement = idToStatement[statementId]!!
            when (direction) {
                PathDirection.INCOMING -> listOf(statement.`object`, statement.predicate)
                PathDirection.OUTGOING -> listOf(statement.subject, statement.predicate)
                PathDirection.UNDIRECTED -> throw IllegalArgumentException()
            }
        } + statementIds.last().let { (id, direction) ->
            val statementId = StatementId(id)
            val statement = idToStatement[statementId]!!
            when (direction) {
                PathDirection.INCOMING -> statement.subject
                PathDirection.OUTGOING -> statement.`object`
                PathDirection.UNDIRECTED -> throw IllegalArgumentException()
            }
        }
    }

    describe("fetching all paths by id") {
        val testGraph = createTestGraph()
        describe("without filters") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S1"),
                testGraph.pathOf(+"S1", +"S2"),
                testGraph.pathOf(+"S1", +"S2", +"S3"),
                testGraph.pathOf(+"S4"),
                testGraph.pathOf(+"S4", +"S5"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
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
        }
        describe("with min hops") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S1", +"S2", +"S3"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                minHops = 3,
                maxHops = 3,
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
        }
        describe("with max hops") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S1"),
                testGraph.pathOf(+"S1", +"S2"),
                testGraph.pathOf(+"S4"),
                testGraph.pathOf(+"S4", +"S5"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                maxHops = 2,
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
        }
        describe("with deny classes") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S1"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                denyClasses = setOf(ThingId("C2"), ThingId("C3")),
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
        }
        describe("with allow classes") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S1"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                allowClasses = setOf(ThingId("C1")),
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
        }
        describe("with termination classes") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S4"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                terminationClasses = setOf(ThingId("C3")),
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
        }
        describe("with path direction (incoming)") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(-"S3"),
                testGraph.pathOf(-"S3", -"S2"),
                testGraph.pathOf(-"S3", -"S2", -"S1"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                pathDirection = PathDirection.INCOMING,
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
        }
        describe("with path direction (undirected)") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S1"),
                testGraph.pathOf(-"S3"),
                testGraph.pathOf(+"S4"),
                testGraph.pathOf(+"S4", +"S5"),
                testGraph.pathOf(-"S3", -"S2"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                pathDirection = PathDirection.UNDIRECTED,
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
        }
        describe("without including root") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(+"S1"),
                testGraph.pathOf(+"S1", +"S2"),
                testGraph.pathOf(+"S1", +"S2", +"S3"),
                testGraph.pathOf(+"S4"),
                testGraph.pathOf(+"S4", +"S5"),
            ).map {
                it.drop(1)
            }
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                includeRoot = false,
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
        }
        describe("with all filters") {
            testGraph.forEach(saveStatement)

            val expected = listOf(
                testGraph.pathOf(-"S3"),
            )
            val pageable = PageRequest.of(0, 10)
            val result = repository.findAllByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                minHops = 1,
                maxHops = 1,
                allowClasses = setOf(ThingId("C1")),
                denyClasses = setOf(ThingId("C3")),
                terminationClasses = setOf(ThingId("C2")),
                pathDirection = PathDirection.UNDIRECTED,
                includeRoot = true,
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
        }
    }
}
