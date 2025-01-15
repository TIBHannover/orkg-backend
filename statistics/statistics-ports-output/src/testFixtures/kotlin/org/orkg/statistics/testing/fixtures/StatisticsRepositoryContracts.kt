package org.orkg.statistics.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.shouldBe
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.withGraphMappings
import org.orkg.statistics.output.StatisticsRepository

fun <
    T : StatisticsRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> statisticsRepositoryContract(
    repository: T,
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
        statementRepository.save(it)
    }

    describe("counting nodes") {
        context("by label") {
            context("without constraints") {
                it("returns the correct result") {
                    val papers = fabricator.random<List<Resource>>()
                        .map { it.copy(classes = setOf(Classes.paper)) }
                    papers.forEach { resourceRepository.save(it) }
                    fabricator.random<List<Resource>>()
                        .forEach { resourceRepository.save(it) }

                    repository.countNodes(Classes.paper.value) shouldBe papers.size.toLong()
                }
            }
        }
    }

    describe("counting relations") {
        context("by type") {
            context("without constraints") {
                it("returns the correct result") {
                    val statements = fabricator.random<List<GeneralStatement>>()
                    statements.forEach(saveStatement)

                    repository.countRelations("RELATED") shouldBe statements.size.toLong()
                }
            }
        }
    }
}
