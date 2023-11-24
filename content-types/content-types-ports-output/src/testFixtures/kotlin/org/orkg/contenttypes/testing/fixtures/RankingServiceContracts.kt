package org.orkg.contenttypes.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.common.ThingId
import org.orkg.contenttypes.output.RankingService
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.ListRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.testing.fixtures.createPredicate
import org.orkg.graph.testing.fixtures.withCustomMappings

fun <
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    I : ListRepository,
    U : RankingService
> rankingServiceContract(
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P,
    @Suppress("UNUSED_PARAMETER") listRepository: I,
    service: U
) = describeSpec {
    beforeTest {
        statementRepository.deleteAll()
        classRepository.deleteAll()
        literalRepository.deleteAll()
        resourceRepository.deleteAll()
        predicateRepository.deleteAll()
        predicateRepository.save(createPredicate(id = Predicates.hasListElement, label = "has list element"))
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

    describe("finding all statements about a paper") {
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Paper"))
        )
        val literalStatements = fabricator.random<List<Literal>>().map {
            fabricator.random<GeneralStatement>().copy(
                subject = paper,
                `object` = it
            )
        }
        literalStatements.forEach(saveStatement)
        val resourceStatements = fabricator.random<List<Resource>>().map {
            fabricator.random<GeneralStatement>().copy(
                subject = paper,
                `object` = it
            )
        }
        resourceStatements.forEach(saveStatement)

        val expected = literalStatements.map { it.predicate.id to it.`object`.id }
            .union(resourceStatements.map { it.predicate.id to it.`object`.id })
        val result = service.findAllStatementsAboutPaper(paper.id)

        it("returns the correct result") {
            result shouldNotBe null
            result.size shouldBe expected.size
            result shouldContainAll expected
        }
    }

    describe("counting sum of distinct predicates for contributions") {
        val sharedPredicate = fabricator.random<Predicate>()
        val contribution1 =  fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Contribution"))
        )
        val statementAboutContribution1 = fabricator.random<GeneralStatement>().copy(
            subject = contribution1,
            predicate = sharedPredicate
        )
        val anotherStatementAboutContribution1 = fabricator.random<GeneralStatement>().copy(
            subject = contribution1,
            predicate = sharedPredicate
        )
        val independentStatementAboutContribution1 = fabricator.random<GeneralStatement>().copy(
            subject = contribution1
        )
        val contribution2 =  fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Contribution"))
        )
        val statementAboutContribution2 = fabricator.random<GeneralStatement>().copy(
            subject = contribution2,
            predicate = sharedPredicate
        )
        val anotherStatementAboutContribution2 = fabricator.random<GeneralStatement>().copy(
            subject = contribution2,
            predicate = sharedPredicate
        )
        val independentStatementAboutContribution2 = fabricator.random<GeneralStatement>().copy(
            subject = contribution2
        )
        saveStatement(statementAboutContribution1)
        saveStatement(anotherStatementAboutContribution1)
        saveStatement(independentStatementAboutContribution1)
        saveStatement(statementAboutContribution2)
        saveStatement(anotherStatementAboutContribution2)
        saveStatement(independentStatementAboutContribution2)

        val expected = 4
        val result = service.countSumOfDistinctPredicatesForContributions(setOf(contribution1.id, contribution2.id))

        it("returns the correct result") {
            result shouldBe expected
        }
    }

    describe("counting comparisons including a paper") {
        val comparison = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Comparison"))
        )
        val contribution = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Contribution"))
        )
        val previousVersionComparison = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Comparison"))
        )
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Paper"))
        )
        val compareContribution = fabricator.random<Predicate>().copy(
            id = ThingId("compareContribution")
        )
        val hasContribution = fabricator.random<Predicate>().copy(
            id = ThingId("P31")
        )
        val hasPreviousVersion = fabricator.random<Predicate>().copy(
            id = ThingId("hasPreviousVersion")
        )
        // create a comparison, with a previous version, that compares one contribution linked by a paper
        saveStatement(fabricator.random<GeneralStatement>().copy(
            subject = comparison,
            predicate = compareContribution,
            `object` = contribution
        ))
        saveStatement(fabricator.random<GeneralStatement>().copy(
            subject = previousVersionComparison,
            predicate = compareContribution,
            `object` = contribution
        ))
        saveStatement(fabricator.random<GeneralStatement>().copy(
            subject = paper,
            predicate = hasContribution,
            `object` = contribution
        ))
        saveStatement(fabricator.random<GeneralStatement>().copy(
            subject = comparison,
            predicate = hasPreviousVersion,
            `object` = previousVersionComparison
        ))

        val expected = 1
        val result = service.countComparisonsIncludingPaper(paper.id)

        it("returns the correct result") {
            result shouldBe expected
        }
    }

    describe("counting literature lists including a paper") {
        val literatureList = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("LiteratureList"))
        )
        val listSection = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("ListSection"))
        )
        val entry = fabricator.random<Resource>()
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(ThingId("Paper"))
        )
        val hasSection = fabricator.random<Predicate>().copy(
            id = ThingId("HasSection")
        )
        val hasEntry = fabricator.random<Predicate>().copy(
            id = ThingId("HasEntry")
        )
        val hasPaper = fabricator.random<Predicate>().copy(
            id = ThingId("HasPaper")
        )
        saveStatement(fabricator.random<GeneralStatement>().copy(
            subject = literatureList,
            predicate = hasSection,
            `object` = listSection
        ))
        saveStatement(fabricator.random<GeneralStatement>().copy(
            subject = listSection,
            predicate = hasEntry,
            `object` = entry
        ))
        saveStatement(fabricator.random<GeneralStatement>().copy(
            subject = entry,
            predicate = hasPaper,
            `object` = paper
        ))

        val expected = 1
        val result = service.countLiteratureListsIncludingPaper(paper.id)

        it("returns the correct result") {
            result shouldBe expected
        }
    }
}
