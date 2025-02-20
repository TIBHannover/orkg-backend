package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.orkg.contenttypes.domain.RosettaStoneStatement
import org.orkg.contenttypes.domain.RosettaStoneStatementVersion
import org.orkg.contenttypes.domain.testing.fixtures.withRosettaStoneStatementMappings
import org.orkg.contenttypes.output.RankingService
import org.orkg.contenttypes.output.RosettaStoneStatementRepository
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
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
import org.orkg.graph.testing.fixtures.withGraphMappings

fun <
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository,
    I : ListRepository,
    T : RosettaStoneStatementRepository,
    U : RankingService,
> rankingServiceContract(
    statementRepository: S,
    classRepository: C,
    literalRepository: L,
    resourceRepository: R,
    predicateRepository: P,
    @Suppress("UNUSED_PARAMETER") listRepository: I,
    rosettaStoneStatementRepository: T,
    service: U,
) = describeSpec {
    beforeTest {
        rosettaStoneStatementRepository.deleteAll()
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
        )
            .withStandardMappings()
            .withGraphMappings()
            .withRosettaStoneStatementMappings()
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

    fun RosettaStoneStatementVersion.requiredEntities(): Set<Thing> =
        subjects.toSet() + objects.flatten()

    fun RosettaStoneStatement.requiredEntities(): Set<Thing> =
        setOfNotNull(
            fabricator.random<Resource>().copy(id = templateId, classes = setOf(Classes.rosettaNodeShape)),
            contextId?.let { fabricator.random<Resource>().copy(id = it) },
            fabricator.random<Class>().copy(id = templateTargetClassId),
        ) + versions.flatMap { it.requiredEntities() }

    describe("finding all statements about a paper") {
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(Classes.paper)
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
        val contribution1 = fabricator.random<Resource>().copy(
            classes = setOf(Classes.contribution)
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
        val contribution2 = fabricator.random<Resource>().copy(
            classes = setOf(Classes.contribution)
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
        val publishedComparison = fabricator.random<Resource>().copy(
            classes = setOf(Classes.comparisonPublished, Classes.latestVersion)
        )
        val contribution = fabricator.random<Resource>().copy(
            classes = setOf(Classes.contribution)
        )
        val headComparison = fabricator.random<Resource>().copy(
            classes = setOf(Classes.comparison)
        )
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(Classes.paper)
        )
        val compareContribution = fabricator.random<Predicate>().copy(
            id = Predicates.comparesContribution
        )
        val hasContribution = fabricator.random<Predicate>().copy(
            id = Predicates.hasContribution
        )
        val hasPublishedVersion = fabricator.random<Predicate>().copy(
            id = Predicates.hasPublishedVersion
        )
        // create a head comparison linked to a published version, that
        // both compare the same contribution, that is part of a paper
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = publishedComparison,
                predicate = compareContribution,
                `object` = contribution
            )
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = headComparison,
                predicate = compareContribution,
                `object` = contribution
            )
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = paper,
                predicate = hasContribution,
                `object` = contribution
            )
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = headComparison,
                predicate = hasPublishedVersion,
                `object` = publishedComparison
            )
        )

        val expected = 1
        val result = service.countComparisonsIncludingPaper(paper.id)

        it("returns the correct result") {
            result shouldBe expected
        }
    }

    describe("counting literature lists including a paper") {
        val literatureList = fabricator.random<Resource>().copy(
            classes = setOf(Classes.literatureList)
        )
        val listSection = fabricator.random<Resource>().copy(
            classes = setOf(Classes.listSection)
        )
        val entry = fabricator.random<Resource>()
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(Classes.paper)
        )
        val hasSection = fabricator.random<Predicate>().copy(
            id = Predicates.hasSection
        )
        val hasEntry = fabricator.random<Predicate>().copy(
            id = Predicates.hasEntry
        )
        val hasPaper = fabricator.random<Predicate>().copy(
            id = Predicates.hasLink
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = literatureList,
                predicate = hasSection,
                `object` = listSection
            )
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = listSection,
                predicate = hasEntry,
                `object` = entry
            )
        )
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = entry,
                predicate = hasPaper,
                `object` = paper
            )
        )

        val expected = 1
        val result = service.countLiteratureListsIncludingPaper(paper.id)

        it("returns the correct result") {
            result shouldBe expected
        }
    }

    describe("counting rosetta stone statement associated to a paper") {
        val paper = fabricator.random<Resource>().copy(
            classes = setOf(Classes.paper)
        )
        val associatedStatement: RosettaStoneStatement = fabricator.random<RosettaStoneStatement>().copy(
            contextId = paper.id
        )
        associatedStatement.requiredEntities().forEach(saveThing)
        rosettaStoneStatementRepository.save(associatedStatement)

        val notAssociatedStatement: RosettaStoneStatement = fabricator.random<RosettaStoneStatement>()
        notAssociatedStatement.requiredEntities().forEach(saveThing)
        rosettaStoneStatementRepository.save(notAssociatedStatement)

        val expected = 1
        val result = service.countRosettaStoneStatementsAssociatedToPaper(paper.id)

        it("returns the correct result") {
            result shouldBe expected
        }
    }
}
