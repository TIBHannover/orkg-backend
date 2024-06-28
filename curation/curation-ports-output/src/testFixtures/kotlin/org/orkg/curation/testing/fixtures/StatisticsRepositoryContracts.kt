package org.orkg.curation.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.graph.domain.Class
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
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.orkg.curation.output.CurationRepository
import org.orkg.graph.domain.Predicates
import org.springframework.data.domain.PageRequest

fun <
    T : CurationRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> curationRepositoryContract(
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

    describe("listing predicates") {
        context("without description") {
            val predicates: List<Predicate> = fabricator.random<List<Predicate>>()
            predicates.forEach(predicateRepository::save)

            val description = fabricator.random<Predicate>().copy(id = Predicates.description)

            predicates.take(6).forEach { predicate ->
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = predicate,
                        predicate = description,
                        `object` = fabricator.random<Literal>()
                    )
                )
            }

            val expected = (predicates.drop(6) + description).sortedBy { it.createdAt }
            val result = repository.findAllPredicatesWithoutDescriptions(PageRequest.of(0, 10))

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

    describe("listing classes") {
        context("without description") {
            val classes: List<Class> = fabricator.random<List<Class>>()
            classes.forEach(classRepository::save)

            val description = fabricator.random<Predicate>().copy(id = Predicates.description)

            classes.take(6).forEach { item ->
                saveStatement(
                    fabricator.random<GeneralStatement>().copy(
                        subject = item,
                        predicate = description,
                        `object` = fabricator.random<Literal>()
                    )
                )
            }

            val expected = (classes.drop(6) + description).sortedBy { it.createdAt }
            val result = repository.findAllClassesWithoutDescriptions(PageRequest.of(0, 10))

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
