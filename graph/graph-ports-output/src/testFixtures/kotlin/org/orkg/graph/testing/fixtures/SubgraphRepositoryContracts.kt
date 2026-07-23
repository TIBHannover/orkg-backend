package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ThingId
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.StatementId
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.SubgraphRepository
import org.springframework.data.domain.PageRequest
import java.time.OffsetDateTime

fun <
    T : SubgraphRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
> subgraphRepositoryContract(
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

    val fabricator = Fabrikate(
        FabricatorConfig(
            collectionSizes = 12..12,
            nullableStrategy = FabricatorConfig.NullableStrategy.NeverSetToNull, // FIXME: because "id" is nullable
        )
            .withStandardMappings()
            .withGraphMappings(),
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
        statementRepository.save(it)
    }

    // r4 ← r1 → r2
    // ↓     ↑   ↓
    // r5     r3
    val createTestGraph: () -> List<GeneralStatement> = createTestGraph@{
        val r1 = createResource(id = ThingId("R1"), classes = setOf(ThingId("C1")))
        val r2 = createResource(id = ThingId("R2"), classes = setOf(ThingId("C1")))
        val r3 = createResource(id = ThingId("R3"), classes = setOf(ThingId("C2")))
        val r4 = createResource(id = ThingId("R4"), classes = setOf(ThingId("C3")))
        val r5 = createResource(id = ThingId("R5"), classes = setOf(ThingId("C4")))
        val createdAt: OffsetDateTime = fabricator.random()
        listOf(
            createStatement(id = StatementId("S1"), subject = r1, `object` = r2, createdAt = createdAt.plusHours(1)),
            createStatement(id = StatementId("S2"), subject = r2, `object` = r3, createdAt = createdAt.plusHours(2)),
            createStatement(id = StatementId("S3"), subject = r3, `object` = r1, createdAt = createdAt.plusHours(3)),
            createStatement(id = StatementId("S4"), subject = r1, `object` = r4, createdAt = createdAt.plusHours(4)),
            createStatement(id = StatementId("S5"), subject = r4, `object` = r5, createdAt = createdAt.plusHours(5)),
        )
    }

    describe("fetching a subgraph") {
        val testGraph = createTestGraph()
        describe("without filters") {
            testGraph.forEach(saveStatement)

            val pageable = PageRequest.of(0, 10)
            val result = repository.findByRootId(
                id = ThingId("R1"),
                pageable = pageable,
            )

            it("returns the correct result") {
                result shouldNotBe null
                result.content shouldNotBe null
                result.content.size shouldBe testGraph.size
                result.content shouldContainAll testGraph
            }
            it("pages the result correctly") {
                result.size shouldBe 10
                result.number shouldBe 0
                result.totalPages shouldBe 1
                result.totalElements shouldBe testGraph.size
            }
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        describe("with min hops") {
            testGraph.forEach(saveStatement)

            val expected = listOf(testGraph[0], testGraph[1], testGraph[2])
            val pageable = PageRequest.of(0, 10)
            val result = repository.findByRootId(
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
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        describe("with max hops") {
            testGraph.forEach(saveStatement)

            val expected = listOf(testGraph[0], testGraph[1], testGraph[3], testGraph[4])
            val pageable = PageRequest.of(0, 10)
            val result = repository.findByRootId(
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
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        describe("with deny classes") {
            testGraph.forEach(saveStatement)

            val expected = listOf(testGraph[0])
            val pageable = PageRequest.of(0, 10)
            val result = repository.findByRootId(
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
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        describe("with allow classes") {
            testGraph.forEach(saveStatement)

            val expected = listOf(testGraph[0])
            val pageable = PageRequest.of(0, 10)
            val result = repository.findByRootId(
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
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        describe("with termination classes") {
            testGraph.forEach(saveStatement)

            val expected = listOf(testGraph[3])
            val pageable = PageRequest.of(0, 10)
            val result = repository.findByRootId(
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
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
        describe("with all filters") {
            testGraph.forEach(saveStatement)

            val expected = listOf(testGraph[3])
            val pageable = PageRequest.of(0, 10)
            val result = repository.findByRootId(
                id = ThingId("R1"),
                pageable = pageable,
                minHops = 1,
                maxHops = 1,
                allowClasses = setOf(ThingId("C1")),
                denyClasses = setOf(ThingId("C2")),
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
            it("sorts the results by creation date by default") {
                result.content.zipWithNext { a, b ->
                    a.createdAt!! shouldBeLessThan b.createdAt!!
                }
            }
        }
    }
}
