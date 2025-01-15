package org.orkg.graph.testing.fixtures

import dev.forkhandles.fabrikate.FabricatorConfig
import dev.forkhandles.fabrikate.Fabrikate
import io.kotest.assertions.asClue
import io.kotest.core.spec.style.describeSpec
import io.kotest.matchers.shouldBe
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LegacyStatisticsRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository

fun <
    T : LegacyStatisticsRepository,
    S : StatementRepository,
    C : ClassRepository,
    L : LiteralRepository,
    R : ResourceRepository,
    P : PredicateRepository
> legacyStatisticsRepositoryContract(
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

    val createStatement: (Thing, Predicate, Thing) -> Unit = { subject, predicate, `object` ->
        saveStatement(
            fabricator.random<GeneralStatement>().copy(
                subject = subject,
                predicate = predicate,
                `object` = `object`
            )
        )
    }

    describe("finding research field stats") {
        context("by id") {
            val r1 = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
            val r2 = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))
            val r3 = fabricator.random<Resource>().copy(classes = setOf(Classes.researchField))

            val hasSubfield = fabricator.random<Predicate>().copy(id = Predicates.hasSubfield)

            createStatement(r1, hasSubfield, r2)
            createStatement(r2, hasSubfield, r3)

            val ppr1 = fabricator.random<Resource>().copy(classes = setOf(Classes.paper))
            val ppr2 = fabricator.random<Resource>().copy(classes = setOf(Classes.paper))
            val ppr3 = fabricator.random<Resource>().copy(classes = setOf(Classes.paper))
            val ppr4 = fabricator.random<Resource>().copy(classes = setOf(Classes.paper))

            val hasResearchField = fabricator.random<Predicate>().copy(id = Predicates.hasResearchField)

            createStatement(ppr1, hasResearchField, r1)
            createStatement(ppr2, hasResearchField, r2)
            createStatement(ppr3, hasResearchField, r3)
            createStatement(ppr4, hasResearchField, r3)

            val hasSubject = fabricator.random<Predicate>().copy(id = Predicates.hasSubject)
            val hasPublishedVersion = fabricator.random<Predicate>().copy(id = Predicates.hasPublishedVersion)

            val cmp1 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparison))
            val cmp2 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparison))
            val cmp3 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparison))

            createStatement(cmp1, hasSubject, r1)
            createStatement(cmp2, hasSubject, r2)
            createStatement(cmp3, hasSubject, r3)

            val publishedCmp1 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparisonPublished, Classes.latestVersion))
            val publishedCmp2 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparisonPublished, Classes.latestVersion))
            val publishedCmp3 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparisonPublished, Classes.latestVersion))

            createStatement(publishedCmp1, hasSubject, r1)
            createStatement(publishedCmp2, hasSubject, r1)
            createStatement(publishedCmp2, hasSubject, r2)
            createStatement(publishedCmp3, hasSubject, r3)

            createStatement(cmp1, hasPublishedVersion, publishedCmp1)
            createStatement(cmp2, hasPublishedVersion, publishedCmp2)
            createStatement(cmp3, hasPublishedVersion, publishedCmp3)

            val oldCmp1 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparisonPublished))
            val oldCmp2 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparisonPublished))
            val oldCmp3 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparisonPublished))

            createStatement(oldCmp1, hasSubject, r1)
            createStatement(oldCmp2, hasSubject, r2)
            createStatement(oldCmp3, hasSubject, r3)

            createStatement(cmp1, hasPublishedVersion, oldCmp1)
            createStatement(cmp2, hasPublishedVersion, oldCmp2)
            createStatement(cmp3, hasPublishedVersion, oldCmp3)

            val excludingSubfields = repository.findResearchFieldStatsById(r1.id, false)
            val includingSubfields = repository.findResearchFieldStatsById(r1.id, true)

            context("excluding subfields") {
                it("returns the correct result") {
                    excludingSubfields.isPresent shouldBe true
                    excludingSubfields.get().asClue {
                        it.id shouldBe r1.id
                        it.papers shouldBe 1
                        it.comparisons shouldBe 2
                        it.total shouldBe 3
                    }
                }
            }
            context("including subfields") {
                it("returns the correct result") {
                    includingSubfields.isPresent shouldBe true
                    includingSubfields.get().asClue {
                        it.id shouldBe r1.id
                        it.papers shouldBe 4
                        it.comparisons shouldBe 3
                        it.total shouldBe 7
                    }
                }
            }
        }
    }
}
