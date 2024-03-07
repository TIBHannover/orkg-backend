package org.orkg.community.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.*
import org.orkg.common.ObservatoryId
import org.orkg.common.ThingId
import org.orkg.graph.domain.SearchFilter
import org.orkg.graph.domain.SearchFilter.Operator
import org.orkg.graph.domain.SearchFilter.Value
import org.orkg.community.output.ObservatoryResourceRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.domain.VisibilityFilter
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createLiteral
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.createResource
import org.orkg.graph.testing.fixtures.createStatement
import org.orkg.graph.testing.fixtures.withCustomMappings
import org.springframework.data.domain.PageRequest

fun <
    O : ObservatoryResourceRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> observatoryResourceRepositoryContract(
    repository: O,
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

    describe("finding several papers") {
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
                        path = listOf(ThingId("P32")),
                        range = ThingId("Resources"),
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
}
