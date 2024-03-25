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
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import org.orkg.graph.output.LegacyStatisticsRepository

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

            val cmp1 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparison))
            val cmp2 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparison))
            val cmp3 = fabricator.random<Resource>().copy(classes = setOf(Classes.comparison))

            createStatement(cmp1, hasResearchField, r1)
            createStatement(cmp3, hasResearchField, r3)

            val ctb1 = fabricator.random<Resource>().copy(classes = setOf(Classes.contribution))
            val ctb2 = fabricator.random<Resource>().copy(classes = setOf(Classes.contribution))
            val ctb3 = fabricator.random<Resource>().copy(classes = setOf(Classes.contribution))

            val hasContribution = fabricator.random<Predicate>().copy(id = Predicates.hasContribution)
            val comparesContribution = fabricator.random<Predicate>().copy(id = Predicates.comparesContribution)

            createStatement(cmp1, comparesContribution, ctb1)
            createStatement(ppr1, hasContribution, ctb1)

            createStatement(cmp2, comparesContribution, ctb2)
            createStatement(ppr1, hasContribution, ctb2)
            createStatement(ppr4, hasContribution, ctb2)

            createStatement(cmp3, comparesContribution, ctb3)
            createStatement(ppr3, hasContribution, ctb3)

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
