package org.orkg.contenttypes.output.testing.fixtures

import dev.forkhandles.fabrikate.Fabrikate
import org.orkg.common.ThingId
import org.orkg.common.testing.fixtures.fixedClock
import org.orkg.graph.domain.Class
import org.orkg.graph.domain.Classes
import org.orkg.graph.domain.GeneralStatement
import org.orkg.graph.domain.Literal
import org.orkg.graph.domain.Predicate
import org.orkg.graph.domain.Predicates
import org.orkg.graph.domain.Resource
import org.orkg.graph.domain.Thing
import org.orkg.graph.domain.Visibility
import org.orkg.graph.output.ClassRepository
import org.orkg.graph.output.LiteralRepository
import org.orkg.graph.output.PredicateRepository
import org.orkg.graph.output.ResourceRepository
import org.orkg.graph.output.StatementRepository
import java.time.OffsetDateTime

internal class PublicationTestGraphFactory(
    private val fabricator: Fabrikate,
    private val statementRepository: StatementRepository,
    private val classRepository: ClassRepository,
    private val literalRepository: LiteralRepository,
    private val resourceRepository: ResourceRepository,
    private val predicateRepository: PredicateRepository,
    private val unpublishedClassId: ThingId,
    private val publishedClassId: ThingId,
) {
    internal inner class TestGraph(
        val resources: List<Resource>,
        val statements: List<GeneralStatement>,
        val ignored: Set<Resource>,
    ) {
        val expected: List<Resource> get() = (resources - ignored)

        fun save(): TestGraph {
            resources.forEach(resourceRepository::save)
            statements.forEach(::saveStatement)
            return this
        }
    }

    private fun saveThing(thing: Thing) {
        when (thing) {
            is Class -> classRepository.save(thing)
            is Literal -> literalRepository.save(thing)
            is Resource -> resourceRepository.save(thing)
            is Predicate -> predicateRepository.save(thing)
        }
    }

    private fun saveStatement(it: GeneralStatement) {
        saveThing(it.subject)
        saveThing(it.predicate)
        saveThing(it.`object`)
        statementRepository.save(it)
    }

    // (unpublished 1)
    // (unpublished 2)
    // (unpublished 3) -> (published 1)
    // (unpublished 4) -> (published 2)
    // (unpublished 5) -> (published 3 + 4)
    // (unpublished 6) -> (published 5 + 6)
    internal fun create(
        transform: (Int, Resource) -> Resource = { _, it -> it.copy(visibility = Visibility.DEFAULT) },
    ): TestGraph {
        val resources = fabricator.random<List<Resource>>().mapIndexed(transform)
        val unpublished = resources.take(6).map { it.copy(classes = setOf(unpublishedClassId)) }
        val published = resources.drop(6).mapIndexed { index, it ->
            it.copy(
                classes = setOf(publishedClassId, Classes.latestVersion),
                createdAt = OffsetDateTime.now(fixedClock).minusHours(index.toLong()),
            )
        }.toMutableList()
        val statements = mutableListOf<GeneralStatement>()
        val hasPublishedVersion = fabricator.random<Predicate>().copy(id = Predicates.hasPublishedVersion)
        val ignored = mutableSetOf<Resource>()
        // link two published entities to an unpublished entity (2x)
        for (i in 0..1) {
            for (j in 0..1) {
                if (j > 0) {
                    // published, but outdated, so we want to ignore them later
                    val outdated = published[2 + i * 2 + j].let {
                        it.copy(classes = it.classes - Classes.latestVersion)
                    }
                    published[2 + i * 2 + j] = outdated
                    ignored.add(outdated)
                }
                statements.add(
                    fabricator.random<GeneralStatement>().copy(
                        subject = unpublished[4 + i],
                        predicate = hasPublishedVersion,
                        `object` = published[2 + i * 2 + j],
                    ),
                )
            }
        }
        // link a single published entity to an unpublished entity (2x)
        for (i in 0..1) {
            statements.add(
                fabricator.random<GeneralStatement>().copy(
                    subject = unpublished[2 + i],
                    predicate = hasPublishedVersion,
                    `object` = published[i],
                ),
            )
        }
        return TestGraph(unpublished + published, statements, ignored)
    }
}
